package api.response;

import order.Order;

import java.util.Set;

/**
 * GSON class for notifications
 */
public class Notification extends Response {
    @SuppressWarnings("unused")
    private final String notification = "closedTrades";
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Set<Order> trades;

    public Notification(Set<Order> trades) {
        this.trades = trades;
    }
}
