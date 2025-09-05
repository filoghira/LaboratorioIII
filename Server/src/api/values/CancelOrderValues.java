package api.values;

/**
 * GSON class for the possible values of a cancelOrder request
 */
public class CancelOrderValues extends Values{
    private final int orderId;

    public CancelOrderValues(int id) {
        this.orderId = id;
    }

    public int getOrderId() {
        return orderId;
    }
}
