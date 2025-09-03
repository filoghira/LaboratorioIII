package api;

public class CancelOrderValues { //classe che contiene i valori per operazioni cancelOrder
    private final int orderId;

    public CancelOrderValues(int id) {
        this.orderId = id;
    }

    public int getOrderId() {
        return orderId;
    }
}
