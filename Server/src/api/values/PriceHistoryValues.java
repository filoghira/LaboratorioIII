package api.values;

/**
 * GSON class for the possible values of a getPriceHistory request
 */
public class PriceHistoryValues extends Values {
    private final String month;

    public PriceHistoryValues(String m) {
        this.month = m;
    }

    public String getMonth() {
        return month;
    }
}
