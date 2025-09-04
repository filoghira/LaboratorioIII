package database;

import order.Order;
import java.util.concurrent.ConcurrentHashMap;

public class LoadOrdersReturnValue {
    private final int maxId;
    private final ConcurrentHashMap<Integer, Order> orders;

    public LoadOrdersReturnValue(int maxId, ConcurrentHashMap<Integer, Order> orders) {
        this.maxId = maxId;
        this.orders = orders;
    }

    public ConcurrentHashMap<Integer, Order> getOrders() {
        return orders;
    }

    public int getMaxId() {
        return maxId;
    }
}
