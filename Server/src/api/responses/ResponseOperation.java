package api.responses;

/**
 * GSON class for a response among insertMarketOrder, insertLimitOrder and insertStopOrder
 */
public class ResponseOperation extends Response {
    private final int orderID;

    public ResponseOperation(int id) {
        this.orderID = id;
    }

    public int getOrderID() {
        return this.orderID;
    }
}
