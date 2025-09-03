package order;

import api.values.Day;
import api.values.OrderDirection;
import api.values.OrderType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import customExceptions.*;
import user.User;

import java.io.InputStream;
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
        this.orders = new ConcurrentHashMap<>();
        this.lastOrderID = new AtomicInteger(0);
        this.stopOrdersAsk = new ConcurrentHashMap<>();
        this.stopOrdersBid = new ConcurrentHashMap<>();
        this.askOrders = new ConcurrentSkipListSet<>(Comparator.comparingInt(OrderGroup::getPrice));
        this.bidOrders = new ConcurrentSkipListSet<>(Comparator.comparingInt(OrderGroup::getPrice).reversed());
    }

    public void executeOrder(Order order) throws OrderNotExecutableException {
        if (order == null || order.isDone()) return;

        if(order.getOrderType() == STOP || order.getOrderType() == MARKET) {
            Iterator<OrderGroup> it = (order.getDirection() == OrderDirection.ASK ? bidOrders : askOrders).iterator();
            int sum = 0;
            while (it.hasNext()) {
                OrderGroup group = it.next();
                sum += group.getSize();
            }

            if (sum < order.getRemainingSize()) {
                throw new OrderNotExecutableException();
            }
        }

        Iterator<OrderGroup> it1 = (order.getDirection() == OrderDirection.ASK ? bidOrders : askOrders).iterator();
        Iterator<OrderGroup> it2 = (order.getDirection() == OrderDirection.ASK ? askOrders : bidOrders).iterator();

        ArrayList<Order> doneOrders = new ArrayList<>();

        while (it1.hasNext()) {
            OrderGroup orders = it1.next();

            if(!order.getOrderType().equals(LIMIT) ||
                    (order.getDirection() == OrderDirection.BID && orders.getPrice() <= order.getPrice()) ||
                    (order.getDirection() == OrderDirection.ASK && orders.getPrice() >= order.getPrice())
            ) {
                ArrayList<Integer> IDs = orders.getOrders();
                if(order.getOrderType() == MARKET) order.setPrice(orders.getPrice());

                if(orders.getSize() <= order.getRemainingSize()) {
                    IDs.forEach(id -> doneOrders.add(this.orders.get(id)));

                    IDs.forEach(id -> this.orders.get(id).execute());
                    IDs.forEach(id -> this.orders.get(id).setTimestamp(new Date().getTime()));

                    order.updateRemainingSize(orders.getSize());

                    if(order.getDirection() == OrderDirection.BID) {
                        askOrders.remove(orders);
                    } else {
                        bidOrders.remove(orders);
                    }
                } {
                    for (Integer id : IDs) {
                        Order o = this.orders.get(id);
                        if (o.getRemainingSize() <= order.getRemainingSize()) {
                            doneOrders.add(o);
                            o.execute();
                            o.setTimestamp(new Date().getTime());
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
                doneOrders.add(order);
            } else {
                boolean check = true;
                while (it2.hasNext()) {
                    OrderGroup orders = it2.next();
                    if (orders.getPrice() == order.getPrice()) {
                        orders.setSize(orders.getSize() + order.getRemainingSize());
                        orders.getOrders().add(order.getOrderID());
                        check = false;
                        break;
                    }
                }

                if (check) {
                    OrderGroup orders = new OrderGroup(order.getPrice(), order.getRemainingSize(), order);
                    if (order.getDirection() == OrderDirection.ASK) {
                        askOrders.add(orders);
                    } else {
                        bidOrders.add(orders);
                    }
                }
            }
        } else {
            order.execute();
            order.setTimestamp(new Date().getTime());
            doneOrders.add(order);
        }

        // TODO: Notification to users about executed orders

        checkStopOrders();
    }

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
                for (Integer order : stopOrdersBid.get(price)) {
                    executeOrder(orders.get(order));
                }
            }
        }
    }

    public int insertLimitOrder(OrderDirection orderDirection, int size, int price, String username) throws IllegalOrderSizeException, IllegalOrderPriceException, OrderNotExecutableException {
        if (size <= 0) {
            throw new IllegalOrderSizeException();
        }

        if (price <= 0) {
            throw new IllegalOrderPriceException();
        }

        int ID = lastOrderID.incrementAndGet();
        Order order = new Order(ID, orderDirection, OrderType.LIMIT, size, price, username);
        orders.put(ID, order);

        executeOrder(order);
        return ID;
    }

    public int insertMarketOrder(OrderDirection orderDirection, int size, String username) throws IllegalOrderSizeException {
        if (size <= 0) {
            throw new IllegalOrderSizeException();
        }

        int ID = lastOrderID.incrementAndGet();
        Order order = new Order(ID, orderDirection, MARKET, size, 0, username);
        orders.put(ID, order);

        return ID;
    }

    public int insertStopOrder(OrderDirection orderDirection, int size, int price, String username) throws IllegalOrderSizeException, IllegalOrderPriceException, OrderNotExecutableException {
        if (size <= 0) {
            throw new IllegalOrderSizeException();
        }

        if (price <= 0) {
            throw new IllegalOrderPriceException();
        }

        int ID = lastOrderID.incrementAndGet();
        Order order = new Order(ID, orderDirection, STOP, size, price, username);
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
        switch (order.getDirection()) {
            case ASK:
                stopOrdersAsk.get(order.getPrice()).remove((Integer) order.getOrderID());
                if (stopOrdersAsk.get(order.getPrice()).isEmpty()) {
                    stopOrdersAsk.remove(order.getPrice());
                }
                break;
            case BID:
                stopOrdersBid.get(order.getPrice()).remove((Integer) order.getOrderID());
                if (stopOrdersBid.get(order.getPrice()).isEmpty()) {
                    stopOrdersBid.remove(order.getPrice());
                }
                break;
        }
    }

    private void removeLimit(Order order) {
        switch (order.getDirection()) {
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

    public void cancelOrder(int orderID, User user) throws WrongUserException, OrderAlreadyCompletedException {
        Order order = orders.get(orderID);

        if (order == null) return;

        if (user == null || !order.getUsername().equals(user.getUsername())) {
            throw new WrongUserException(user==null ? "null" : user.getUsername());
        }

        if (order.isDone()) {
            throw new OrderAlreadyCompletedException(order.getOrderID());
        }

        orders.remove(order.getOrderID());
        switch (order.getOrderType()) {
            case STOP:
                removeStop(order);
                break;
            case LIMIT:
                removeLimit(order);
                break;
            case MARKET:
                throw new OrderAlreadyCompletedException(order.getOrderID());
        }
    }

    public ConcurrentHashMap<Integer, Day> getPriceHistory(YearMonth date, User user) {
        if (user == null) return null;
        ConcurrentHashMap<Integer, Day> history = new ConcurrentHashMap<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classloader.getResourceAsStream("priceHistory.json")) {
            if (is == null) throw new Exception("File not found");
            Scanner scanner = new Scanner(is);

            // Skip the first two lines (the opening bracket and the "trades" key)
            scanner.nextLine();
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Order order = gson.fromJson(line.charAt(0) == ',' ? line.substring(1) : line, Order.class);
                LocalDateTime orderDate = LocalDateTime.ofEpochSecond(order.getTimestamp(), 0, ZoneOffset.of("+02:00"));

                if (orderDate.getYear() == date.getYear() && orderDate.getMonth() == date.getMonth()) {
                    Day day = history.get(orderDate.getDayOfMonth());
                    if (day != null) {
                        if (order.getPrice() > day.getMaxPrice()) {
                            day.setMaxPrice(order.getPrice());
                        }
                        if (order.getPrice() < day.getMinPrice()) {
                            day.setMinPrice(order.getPrice());
                        }
                        day.setClosingPrice(order.getPrice());
                    } else {
                        day = new Day(orderDate.getDayOfMonth(), order.getPrice(), order.getPrice(), order.getPrice(), order.getPrice());
                        history.put(orderDate.getDayOfMonth(), day);
                    }
                }
            }

        } catch (Exception e) {
            logger.severe("Error reading price history: " + e.getMessage());
            return null;
        }

        return history;
    }
}
