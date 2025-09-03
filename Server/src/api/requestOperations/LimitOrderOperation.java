package api.requestOperations;

import api.values.LimitAndStopOrderValues;

public class LimitOrderOperation extends Operation { //classe per le operazioni insertLimitOrder
    private final LimitAndStopOrderValues values;

    public LimitOrderOperation(String o, LimitAndStopOrderValues v) {
        super(o);
        this.values = v;
    }

    public LimitAndStopOrderValues getValues() {
        return values;
    }
}
