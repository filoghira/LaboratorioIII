package api.request;

import api.values.LimitAndStopOrderValues;

/**
 * GSON class for insertStopOrder operation
 */
public class StopOrderOperation extends Operation {
    private final LimitAndStopOrderValues values;

    public StopOrderOperation(String o, LimitAndStopOrderValues v) {
        super(o);
        this.values = v;
    }

    public LimitAndStopOrderValues getValues() {
        return values;
    }
}
