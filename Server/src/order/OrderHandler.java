package order;

import api.responses.Notification;
import api.values.Day;
import api.values.OrderDirection;
import api.values.OrderType;
import customExceptions.*;
import database.DumbDatabase;
import user.User;
import notification.NotificationHandler;

import java.io.FileNotFoundException;
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

    public synchronized void executeOrder(Order order) throws OrderNotExecutableException {
        if (order == null || order.isDone()) return;

        // TODO: deve essere un set per evitare duplicati
        Set<User> involvedUsers = new HashSet<>();

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

        Set<Order> doneOrders = new HashSet<>();

        while (it1.hasNext()) {
            OrderGroup orders = it1.next();

            if(!order.getOrderType().equals(LIMIT) ||
                    (order.getType() == OrderDirection.BID && orders.getPrice() <= order.getPrice()) ||
                    (order.getType() == OrderDirection.ASK && orders.getPrice() >= order.getPrice())
            ) {
                ArrayList<Integer> IDs = orders.getOrders();
                if(order.getOrderType() == MARKET) order.setPrice(orders.getPrice());

                if(orders.getSize() <= order.getRemainingSize()) {
                    IDs.forEach(id -> doneOrders.add(this.orders.get(id)));

                    IDs.forEach(id -> this.orders.get(id).execute());
                    IDs.forEach(id -> this.orders.get(id).setTimestamp(new Date().getTime()));

                    involvedUsers.add(order.getUser());
                    IDs.forEach(id -> involvedUsers.add(this.orders.get(id).getUser()));

                    order.updateRemainingSize(orders.getSize());

                    if(order.getType() == OrderDirection.BID) {
                        askOrders.remove(orders);
                    } else {
                        bidOrders.remove(orders);
                    }
                } else {
                    for (Integer id : IDs) {
                        Order o = this.orders.get(id);
                        if (o.getRemainingSize() <= order.getRemainingSize()) {
                            doneOrders.add(o);
                            o.execute();
                            o.setTimestamp(new Date().getTime());

                            involvedUsers.add(order.getUser());
                            involvedUsers.add(o.getUser());

                            order.updateRemainingSize(o.getRemainingSize());

                            orders.setSize(order.getRemainingSize() - o.getRemainingSize());
                            orders.getOrders().remove(id);
                        } else {
                            orders.setSize(orders.getSize() - order.getRemainingSize());
                            o.updateRemainingSize(order.getRemainingSize());
                            order.updateRemainingSize(order.getRemainingSize());
                        }
                    }
                }
            }
        }

        if (order.getOrderType() == LIMIT) {
            if (order.getRemainingSize() == 0) {
                order.execute();
                order.setTimestamp(new Date().getTime());
                involvedUsers.add(order.getUser());
                doneOrders.add(order);
            } else {
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
            order.execute();
            order.setTimestamp(new Date().getTime());
            involvedUsers.add(order.getUser());
            doneOrders.add(order);
        }

        for (User u : involvedUsers)
            if (u.isLoggedIn())
                NotificationHandler.sendNotification(new Notification(doneOrders), u.getLastIP());

        checkStopOrders();
    }

    private synchronized void checkStopOrders() throws OrderNotExecutableException {
        ConcurrentHashMap.KeySetView<Integer, ArrayList<Integer>> setAsk = this.stopOrdersAsk.keySet();
        ConcurrentHashMap.KeySetView<Integer, ArrayList<Integer>> setBid = this.stopOrdersBid.keySet();

        ArrayList<String> involvedUsers = new ArrayList<>();

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

    public int insertLimitOrder(OrderDirection orderDirection, int size, int price, User user) throws IllegalOrderSizeException, IllegalOrderPriceException, OrderNotExecutableException {
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

    public int insertMarketOrder(OrderDirection orderDirection, int size, User user) throws IllegalOrderSizeException, OrderNotExecutableException {
        if (size <= 0) {
            throw new IllegalOrderSizeException();
        }

        int ID = lastOrderID.incrementAndGet();
        Order order = new Order(ID, orderDirection, MARKET, size, 0, user);
        orders.put(ID, order);

        logger.log(java.util.logging.Level.INFO, "Inserting market order ID: " + ID + " Direction: " + orderDirection + " Size: " + size + " User: " + (user != null ? user.getUsername() : "null"));

        try {
            executeOrder(order);
            logger.log(java.util.logging.Level.INFO, "Executed market order ID: " + ID);
            return ID;
        } catch (OrderNotExecutableException e) {
            orders.remove(ID);
            throw new OrderNotExecutableException();
        }
    }

    public synchronized int insertStopOrder(OrderDirection orderDirection, int size, int price, User user) throws IllegalOrderSizeException, IllegalOrderPriceException, OrderNotExecutableException {
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

    public synchronized void cancelOrder(int orderID, User user) throws WrongUserException, OrderAlreadyCompletedException, InvalidOrderException {
        Order order = orders.get(orderID);
        logger.log(java.util.logging.Level.INFO, "Attempting to cancel order ID: " + orderID + " by user: " + (user != null ? user.getUsername() : "null"));

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

    public ConcurrentHashMap<Integer, Day> getPriceHistory(YearMonth date, User user) throws FileNotFoundException {
        if (user == null) return null;
        ConcurrentHashMap<Integer, Day> history = new ConcurrentHashMap<>();

        for(Order order : orders.values()) {
            LocalDateTime ldt = LocalDateTime.ofInstant(new Date(order.getTimestamp()).toInstant(), ZoneOffset.UTC);
            if (order.isDone() && YearMonth.from(ldt).equals(date)) {
                int day = ldt.getDayOfMonth();
                if (!history.containsKey(day)) {
                    history.put(day, new Day(day, order.getPrice(), order.getPrice(), order.getPrice(), order.getPrice()));
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
