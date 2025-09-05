package api.request;

import api.values.CancelOrderValues;

/**
 * GSON class for insertCancelOrder operation
 */
public class CancelOrderOperation extends Operation {
    private final CancelOrderValues values;

    public CancelOrderOperation(String o, CancelOrderValues v) {
        super(o);
        this.values = v;
    }

    public CancelOrderValues getValues() {
        return values;
    }
}
