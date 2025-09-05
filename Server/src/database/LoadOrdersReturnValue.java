package database;

import order.Order;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper clas to return:
 * - a HashMap of the Order objects mapped to their orderId
 * - the value of the highest ID
 */
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
