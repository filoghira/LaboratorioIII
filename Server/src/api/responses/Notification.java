package api.responses;

import order.Order;

import java.util.ArrayList;

public class Notification extends Response {
    private final String notification = "closedTrades";
    private final ArrayList<Order> trades;

    public Notification() {
        this.trades = new ArrayList<>();
    }

    public void addTrade(Order order) {
        trades.add(order);
    }
}
