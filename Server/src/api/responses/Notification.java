package api.responses;

import order.Order;

import java.util.ArrayList;

public class Notification extends Response {
    private final String notification = "closedTrades";
    private final ArrayList<Order> trades;

    public Notification(ArrayList<Order> trades) {
        this.trades = trades;
    }

    public void addTrade(Order order) {
        trades.add(order);
    }
}
