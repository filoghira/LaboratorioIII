package order;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

public class OrderGroup {
    private final int price;
    private int size;
    private final ArrayList<Integer> orders;

    public OrderGroup(int price, int size, Order order) {
        this.price = price;
        this.size = size;
        this.orders = new ArrayList<>();
        this.orders.add(order.getOrderId());
    }

    public ArrayList<Integer> getOrders() {
        return orders;
    }

    public int getPrice() {
        return price;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    /**
     * Given a group of orders that are waiting to be executed, if the order that's currently being executed can be
     * deducted, it's removed from the group. Then, if the group is empty, it's removed from the correspondent SkipList
     * (either ASK or BID), otherwise the size of the group is adjusted.
     * @param group Group of orders
     * @param order Order currently being executed
     * @param skipListOrders Either the askOrder or bidOrder skip list
     */
    public static void removeOrderFromGroup(
            OrderGroup group,
            Order order,
            ConcurrentSkipListSet<OrderGroup> skipListOrders
    ) {
        if (group == null | order == null | skipListOrders == null) {
            return;
        }

        if (group.getPrice() >= order.getPrice()) {
            group.getOrders().remove((Integer) order.getOrderId());
            if (group.getOrders().isEmpty()) {
                skipListOrders.remove(group);
            } else {
                group.setSize(group.getSize() - order.getRemainingSize());
            }
        }
    }
}
