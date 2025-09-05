package api.request;

import api.values.LimitAndStopOrderValues;

/**
 * GSON class for insertLimitOrder operation
 */
public class LimitOrderOperation extends Operation {
    private final LimitAndStopOrderValues values;

    public LimitOrderOperation(String o, LimitAndStopOrderValues v) {
        super(o);
        this.values = v;
    }

    public LimitAndStopOrderValues getValues() {
        return values;
    }
}
