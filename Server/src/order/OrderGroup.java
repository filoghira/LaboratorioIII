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
        this.orders.add(order.getOrderID());
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

    public static void removeOrderFromGroup(OrderGroup orders, Order order, ConcurrentSkipListSet<OrderGroup> askOrders) {
        if (orders == null | order == null | askOrders == null) {
            return;
        }

        if (orders.getPrice() >= order.getPrice()) {
            orders.getOrders().remove((Integer) order.getOrderID());
            if (orders.getOrders().isEmpty()) {
                askOrders.remove(orders);
            } else {
                orders.setSize(orders.getSize() - order.getRemainingSize());
            }
        }
    }
}
