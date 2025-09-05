package api.values;

/**
 * GSON class for the possible values of a insertMarketOrder request
 */
public class MarketOrderValues extends InsertOrderValues{

    public MarketOrderValues(OrderDirection t, int s) {
        super(t, s);
    }

}
