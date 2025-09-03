package api.requestOperations;

import api.values.MarketOrderValues;

public class MarketOrderOperation extends Operation { //classe per le operazioni insertMarketOrder
    private final MarketOrderValues values;

    public MarketOrderOperation(String o, MarketOrderValues v) {
        super(o);
        this.values = v;
    }

    public MarketOrderValues getValues() {
        return values;
    }
}
