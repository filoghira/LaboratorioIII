package api.request;

import api.values.PriceHistoryValues;

/**
 * GSON class for getPriceHistory operation
 */
public class PriceHistoryOperation extends Operation {
    private final PriceHistoryValues values;

    public PriceHistoryOperation(String o, PriceHistoryValues v) {
        super(o);
        this.values = v;
    }

    public PriceHistoryValues getValues() {
        return values;
    }
}
