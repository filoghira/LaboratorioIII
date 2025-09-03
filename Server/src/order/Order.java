import api.values.OrderDirection;
import api.values.OrderType;

public class Order {
    private int orderID;
    private OrderDirection direction;
    private OrderType orderType;
    private int size;
    private int price;
    private transient long timestamp;
    private transient int remainingSize;
    private String username;
    private transient boolean done;

    public Order(int orderID, OrderDirection type, OrderType orderType, int size, int price, String username) {
        this.orderID = orderID;
        this.direction = type;
        this.orderType = orderType;
        this.size = size;
        this.price = price;
        this.username = username;
        this.timestamp = System.currentTimeMillis();
        this.remainingSize = size;
        this.done = false;
    }

    public int getPrice() {
        return price;
    }

    public String getUsername() {
        return username;
    }

    public boolean isDone() {
        return done;
    }

    public void execute() {
        this.done = true;
    }

    public int getOrderID() {
        return orderID;
    }

    public OrderDirection getDirection() {
        return direction;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public int getRemainingSize() {
        return remainingSize;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void updateRemainingSize(int executedSize) {
        if (executedSize <= this.remainingSize) {
            this.remainingSize -= executedSize;
        }
    }
}
