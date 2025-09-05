package api.requestOperations;

import api.values.MarketOrderValues;

/**
 * GSON class for insertMarketOrder operation
 */
public class MarketOrderOperation extends Operation {
    private final MarketOrderValues values;

    public MarketOrderOperation(String o, MarketOrderValues v) {
        super(o);
        this.values = v;
    }

    public MarketOrderValues getValues() {
        return values;
    }
}
