package api.responses;

import order.Order;

import java.util.ArrayList;
import java.util.Set;

public class Notification extends Response {
    private final String notification = "closedTrades";
    private final Set<Order> trades;

    public Notification(Set<Order> trades) {
        this.trades = trades;
    }

    public void addTrade(Order order) {
        trades.add(order);
    }
}
