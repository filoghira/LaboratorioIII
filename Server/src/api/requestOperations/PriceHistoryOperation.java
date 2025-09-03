package api.requestOperations;

import api.values.PriceHistoryValues;

public class PriceHistoryOperation extends Operation { //classe per operazioni getPriceHistory
    private final PriceHistoryValues values;

    public PriceHistoryOperation(String o, PriceHistoryValues v) {
        super(o);
        this.values = v;
    }

    public PriceHistoryValues getValues() {
        return values;
    }
}
