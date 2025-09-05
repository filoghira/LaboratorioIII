package order;

import api.response.Notification;
import api.values.Day;
import api.values.OrderDirection;
import api.values.OrderType;
import customExceptions.*;
import database.DumbDatabase;
import user.User;
import notification.NotificationHandler;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static api.values.OrderType.*;

public class OrderHandler {

    private static final Logger logger = Logger.getLogger(OrderHandler.class.getName());

    private final ConcurrentHashMap<Integer, Order> orders;
    private final AtomicInteger lastOrderID;
    private final ConcurrentHashMap<Integer, ArrayList<Integer>> stopOrdersAsk;
    private final ConcurrentHashMap<Integer, ArrayList<Integer>> stopOrdersBid;
    private final ConcurrentSkipListSet<OrderGroup> askOrders;
    private final ConcurrentSkipListSet<OrderGroup> bidOrders;

    public OrderHandler() {
        this.orders = DumbDatabase.loadOrders().getOrders();
        this.lastOrderID = new AtomicInteger(DumbDatabase.loadOrders().getMaxId());
        this.stopOrdersAsk = new ConcurrentHashMap<>();
        this.stopOrdersBid = new ConcurrentHashMap<>();
        this.askOrders = new ConcurrentSkipListSet<>(Comparator.comparingInt(OrderGroup::getPrice));
        this.bidOrders = new ConcurrentSkipListSet<>(Comparator.comparingInt(OrderGroup::getPrice).reversed());
    }

    /**
     * Execute an Order
     * @param order Order to be executed
     * @throws OrderNotExecutableException if the MarketOrder cannot be completed
     */
    public synchronized void executeOrder(Order order) throws OrderNotExecutableException {
        if (order == null || order.isDone()) return;

        // Users involved in all the Orders that get completed in this run
        Set<User> involvedUsers = new HashSet<>();

        // Check if a StopOrder or a MarketOrder can be executed (if there is enough offer or demand)
        if(order.getOrderType() == STOP || order.getOrderType() == MARKET) {
            Iterator<OrderGroup> it = (order.getType() == OrderDirection.ASK ? bidOrders : askOrders).iterator();

            int sum = 0;
            while (it.hasNext()) {
                OrderGroup group = it.next();
                sum += group.getSize();
            }

            if (sum < order.getRemainingSize()) {
                throw new OrderNotExecutableException();
            }
        }

        logger.log(java.util.logging.Level.INFO, "Order ID: " + order.getOrderId() + " is executable");

        Iterator<OrderGroup> it1 = (order.getType() == OrderDirection.ASK ? bidOrders : askOrders).iterator();
        Iterator<OrderGroup> it2 = (order.getType() == OrderDirection.ASK ? askOrders : bidOrders).iterator();

        // All the orders completed in this run
        Set<Order> doneOrders = new HashSet<>();

        while (it1.hasNext()) {
            OrderGroup orders = it1.next();

            // If it's a MarketOrder or a StopOrder and the current group of orders can satisfy it
            if(!order.getOrderType().equals(LIMIT) ||
                    (order.getType() == OrderDirection.BID && orders.getPrice() <= order.getPrice()) ||
                    (order.getType() == OrderDirection.ASK && orders.getPrice() >= order.getPrice())
            ) {
                ArrayList<Integer> IDs = orders.getOrders();
                if(order.getOrderType() == MARKET) order.setPrice(orders.getPrice());

                // If this group of orders is not enough
                if(orders.getSize() <= order.getRemainingSize()) {
                    IDs.forEach(id -> doneOrders.add(this.orders.get(id)));

                    // Execute them
                    IDs.forEach(id -> this.orders.get(id).execute());
                    IDs.forEach(id -> this.orders.get(id).setTimestamp(new Date().getTime()));

                    involvedUsers.add(order.getUser());
                    IDs.forEach(id -> involvedUsers.add(this.orders.get(id).getUser()));

                    order.updateRemainingSize(orders.getSize());

                    // Remove orders with that price
                    if(order.getType() == OrderDirection.BID) {
                        askOrders.remove(orders);
                    } else {
                        bidOrders.remove(orders);
                    }
                } else { // If the current group satisfies the order

                    // For each order in the current group
                    for (Integer id : IDs) {
                        Order o = this.orders.get(id);
                        // If the current order of the current group does not satisfy the main order
                        if (o.getRemainingSize() <= order.getRemainingSize()) {
                            // Complete the current order of the current group
                            doneOrders.add(o);
                            o.execute();
                            o.setTimestamp(new Date().getTime());

                            involvedUsers.add(order.getUser());
                            involvedUsers.add(o.getUser());

                            order.updateRemainingSize(o.getRemainingSize());

                            // Update the main order
                            orders.setSize(order.getRemainingSize() - o.getRemainingSize());

                            // Remove the current order of the current group from the group
                            orders.getOrders().remove(id);
                        } else {
                            // Otherwise update the size of the current group and of the current order
                            // from the current group
                            orders.setSize(orders.getSize() - order.getRemainingSize());
                            o.updateRemainingSize(order.getRemainingSize());
                            order.updateRemainingSize(order.getRemainingSize());
                        }
                    }
                }
            }
        }

        if (order.getOrderType() == LIMIT) {
            // Limit order is satisfied
            if (order.getRemainingSize() == 0) {
                // Execute it
                order.execute();
                order.setTimestamp(new Date().getTime());
                involvedUsers.add(order.getUser());
                doneOrders.add(order);
            } else {
                // Otherwise check if there is a group where the order can be put
                boolean check = true;
                while (it2.hasNext()) {
                    OrderGroup orders = it2.next();
                    if (orders.getPrice() == order.getPrice()) {
                        orders.setSize(orders.getSize() + order.getRemainingSize());
                        orders.getOrders().add(order.getOrderId());
                        check = false;
                        break;
                    }
                }

                // If not, create it
                if (check) {
                    OrderGroup orders = new OrderGroup(order.getPrice(), order.getRemainingSize(), order);
                    if (order.getType() == OrderDirection.ASK) {
                        askOrders.add(orders);
                    } else {
                        bidOrders.add(orders);
                    }
                }
            }
        } else {
            // Stop or Market
            // Execute it
            order.execute();
            order.setTimestamp(new Date().getTime());
            involvedUsers.add(order.getUser());
            doneOrders.add(order);
        }

        // Send a notification to each user involved in this run
        for (User u : involvedUsers)
            if (u.isLoggedIn())
                NotificationHandler.sendNotification(new Notification(doneOrders), u.getLastIP());

        checkStopOrders();
    }

    /**
     * Check if there are StopOrders to be executed
     * @throws OrderNotExecutableException if a non-executable order is found (not enough offer or demand)
     */
    @SuppressWarnings("DuplicatedCode")
    private synchronized void checkStopOrders() throws OrderNotExecutableException {
        ConcurrentHashMap.KeySetView<Integer, ArrayList<Integer>> setAsk = this.stopOrdersAsk.keySet();
        ConcurrentHashMap.KeySetView<Integer, ArrayList<Integer>> setBid = this.stopOrdersBid.keySet();

        if (!askOrders.isEmpty()) {
            int marketPrice = askOrders.first().getPrice();
            for (Integer price : setBid) {
                if(price <= marketPrice) {
                    for (Integer order : stopOrdersAsk.get(price)) {
                        executeOrder(orders.get(order));
                    }
                }
            }
        }

        if (!bidOrders.isEmpty()) {
            int marketPrice = bidOrders.first().getPrice();
            for (Integer price : setAsk) {
                if (price >= marketPrice) {
                    for (Integer order : stopOrdersBid.get(price)) {
                        executeOrder(orders.get(order));
                    }
                }
            }
        }
    }

    /**
     * Insert a LimitOrder
     * @param orderDirection ASK or BID
     * @param size of the order
     * @param price of the order
     * @param user Owner of the order
     * @return ID of the order
     * @throws IllegalOrderSizeException Size must be >= 0
     * @throws IllegalOrderPriceException Price must be >= 0
     * @throws OrderNotExecutableException If the order cannot be executed
     */
    public int insertLimitOrder(OrderDirection orderDirection, int size, int price, User user)
            throws IllegalOrderSizeException, IllegalOrderPriceException, OrderNotExecutableException {
        if (size <= 0) {
            throw new IllegalOrderSizeException();
        }

        if (price <= 0) {
            throw new IllegalOrderPriceException();
        }

        int ID = lastOrderID.incrementAndGet();
        Order order = new Order(ID, orderDirection, OrderType.LIMIT, size, price, user);
        orders.put(ID, order);

        executeOrder(order);
        return ID;
    }

    /**
     * Insert a MarketOrder
     * @param orderDirection ASK or BID
     * @param size of the order
     * @param user Owner of the order
     * @return ID of the order
     * @throws IllegalOrderSizeException Size must be >= 0
     * @throws OrderNotExecutableException If the order cannot be executed
     */
    public int insertMarketOrder(OrderDirection orderDirection, int size, User user)
            throws IllegalOrderSizeException, OrderNotExecutableException {
        if (size <= 0) {
            throw new IllegalOrderSizeException();
        }

        int ID = lastOrderID.incrementAndGet();
        Order order = new Order(ID, orderDirection, MARKET, size, 0, user);
        orders.put(ID, order);

        logger.log(
                java.util.logging.Level.INFO,
                "Inserting market order ID: " + ID +
                        " Direction: " + orderDirection +
                        " Size: " + size +
                        " User: " + (user != null ? user.getUsername() : "null")
        );

        try {
            executeOrder(order);
            logger.log(java.util.logging.Level.INFO, "Executed market order ID: " + ID);
            return ID;
        } catch (OrderNotExecutableException e) {
            orders.remove(ID);
            throw new OrderNotExecutableException();
        }
    }

    /**
     * Insert a StopOrder
     * @param orderDirection ASK or BID
     * @param size of the order
     * @param price of the order
     * @param user Owner of the order
     * @return ID of the order
     * @throws IllegalOrderSizeException Size must be >= 0
     * @throws IllegalOrderPriceException Price must be >= 0
     * @throws OrderNotExecutableException if the order is not executable
     */
    public synchronized int insertStopOrder(OrderDirection orderDirection, int size, int price, User user)
            throws IllegalOrderSizeException, IllegalOrderPriceException, OrderNotExecutableException {
        if (size <= 0) {
            throw new IllegalOrderSizeException();
        }

        if (price <= 0) {
            throw new IllegalOrderPriceException();
        }

        int ID = lastOrderID.incrementAndGet();
        Order order = new Order(ID, orderDirection, STOP, size, price, user);
        orders.put(ID, order);

        switch (orderDirection) {
            case ASK:
                stopOrdersAsk.putIfAbsent(price, new ArrayList<>());
                stopOrdersAsk.get(price).add(ID);
                break;
            case BID:
                stopOrdersBid.putIfAbsent(price, new ArrayList<>());
                stopOrdersBid.get(price).add(ID);
                break;
        }

        checkStopOrders();

        return ID;
    }

    /**
     * Remove a StopOrder
     * @param order to be removed
     */
    private void removeStop(Order order) {
        switch (order.getType()) {
            case ASK:
                stopOrdersAsk.get(order.getPrice()).remove((Integer) order.getOrderId());
                if (stopOrdersAsk.get(order.getPrice()).isEmpty()) {
                    stopOrdersAsk.remove(order.getPrice());
                }
                break;
            case BID:
                stopOrdersBid.get(order.getPrice()).remove((Integer) order.getOrderId());
                if (stopOrdersBid.get(order.getPrice()).isEmpty()) {
                    stopOrdersBid.remove(order.getPrice());
                }
                break;
        }
    }

    /**
     * Remove a LimitOrder
     * @param order to be removed
     */
    private void removeLimit(Order order) {
        switch (order.getType()) {
            case ASK:
                for (OrderGroup orders : askOrders) {
                    OrderGroup.removeOrderFromGroup(orders, order, askOrders);
                    break;
                }

                break;
            case BID:
                for (OrderGroup orders : bidOrders) {
                    OrderGroup.removeOrderFromGroup(orders, order, bidOrders);
                }
                break;
        }
    }

    /**
     * Cancel an existing order
     * @param orderID ID of the order to be canceled
     * @param user User that wants to cancel the order
     * @throws WrongUserException If the user that did the request is not the owner of the order
     * @throws OrderAlreadyCompletedException If the order has already been completed
     * @throws InvalidOrderException If the order is not found
     */
    public synchronized void cancelOrder(int orderID, User user)
            throws WrongUserException, OrderAlreadyCompletedException, InvalidOrderException {
        Order order = orders.get(orderID);
        logger.log(
                java.util.logging.Level.INFO,
                "Attempting to cancel order ID: " + orderID +
                        " by user: " + (user != null ? user.getUsername() : "null")
        );

        if (order == null) throw new InvalidOrderException();

        if (user == null || !order.getUser().getUsername().equals(user.getUsername())) {
            throw new WrongUserException(user==null ? "null" : user.getUsername());
        }

        if (order.isDone()) {
            throw new OrderAlreadyCompletedException(order.getOrderId());
        }

        orders.remove(order.getOrderId());
        switch (order.getOrderType()) {
            case STOP:
                removeStop(order);
                break;
            case LIMIT:
                removeLimit(order);
                break;
            case MARKET:
                throw new OrderAlreadyCompletedException(order.getOrderId());
        }
    }

    /**
     * Get the price history for a specified month
     * @param date Month
     * @param user that requested the price history
     * @return HashMap of Days mapped to the day number in the month
     */
    public ConcurrentHashMap<Integer, Day> getPriceHistory(YearMonth date, User user)  {
        if (user == null) return null;
        ConcurrentHashMap<Integer, Day> history = new ConcurrentHashMap<>();

        for(Order order : orders.values()) {
            LocalDateTime ldt = LocalDateTime.ofInstant(new Date(order.getTimestamp()).toInstant(), ZoneOffset.UTC);
            if (
                    order.isDone() &&
                    YearMonth.from(ldt).equals(date) &&
                    order.getUser().getUsername().equals(user.getUsername())
            ) {
                int day = ldt.getDayOfMonth();
                if (!history.containsKey(day)) {
                    history.put(
                            day,
                            new Day(
                                    day,
                                    order.getPrice(),
                                    order.getPrice(),
                                    order.getPrice(),
                                    order.getPrice()
                            )
                    );
                } else {
                    Day d = history.get(day);
                    d.setClosingPrice(order.getPrice());
                    if (order.getPrice() > d.getMaxPrice()) d.setMaxPrice(order.getPrice());
                    if (order.getPrice() < d.getMinPrice()) d.setMinPrice(order.getPrice());
                }
            }
        }

        return history;
    }

    public ConcurrentHashMap<Integer, Order> getOrders() {
        return orders;
    }
}
