package api.requestOperations;

import api.values.LimitAndStopOrderValues;

public class StopOrderOperation extends Operation { //classe per le operazioni insertStopOrder
    private final LimitAndStopOrderValues values;

    public StopOrderOperation(String o, LimitAndStopOrderValues v) {
        super(o);
        this.values = v;
    }

    public LimitAndStopOrderValues getValues() {
        return values;
    }
}
