package api.requestOperations;

import api.values.CancelOrderValues;

public class CancelOrderOperation extends Operation { //classe per le operazioni cancelOrder
    private final CancelOrderValues values;

    public CancelOrderOperation(String o, CancelOrderValues v) {
        super(o);
        this.values = v;
    }

    public CancelOrderValues getValues() {
        return values;
    }
}
