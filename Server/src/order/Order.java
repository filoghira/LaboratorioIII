package order;

import api.values.OrderDirection;
import api.values.OrderType;
import user.User;

public class Order {
    private final int orderId;
    private final OrderDirection type;
    private final OrderType orderType;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final int size;
    private int price;
    private long timestamp;
    private transient int remainingSize;
    private final transient User user;
    private transient boolean done;

    public Order(int orderID, OrderDirection type, OrderType orderType, int size, int price, User user) {
        this.orderId = orderID;
        this.type = type;
        this.orderType = orderType;
        this.size = size;
        this.price = price;
        this.user = user;
        this.timestamp = System.currentTimeMillis();
        this.remainingSize = size;
        this.done = false;
    }

    public int getPrice() {
        return price;
    }

    public User getUser() {
        return user;
    }

    public boolean isDone() {
        return done;
    }

    public void execute() {
        this.done = true;
    }

    public int getOrderId() {
        return orderId;
    }

    public OrderDirection getType() {
        return type;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public int getRemainingSize() {
        return remainingSize;
    }

    /**
     * @return the UNIX timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp*1000;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * Set the timestamp in UNIX time
     * @param timestamp UNIX time in milliseconds
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp/1000;
    }

    /**
     * Removes the executed amount of an order from the remaining size
     * @param executedSize Amount that has been executed
     */
    public void updateRemainingSize(int executedSize) {
        if (executedSize <= this.remainingSize) {
            this.remainingSize -= executedSize;
        }
    }
}
