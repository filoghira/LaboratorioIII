package api.values;

/**
 * GSON class for the possible values of a insertLimitOrder or insertStopOrder request
 */
public class LimitAndStopOrderValues extends InsertOrderValues{
    private final int price;

    public LimitAndStopOrderValues(OrderDirection t, int s, int p) {
        super(t, s);
        this.price = p;
    }
    public int getPrice() {
        return price;
    }
}
