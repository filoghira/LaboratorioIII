package api.values;

/**
 * GSON class for the result of getPriceHistory
 */
public class Day {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final int day;
    private final int openingPrice;
    private int closingPrice;
    private int maxPrice;
    private int minPrice;

    public Day(int day, int openingPrice, int closingPrice, int maxPrice, int minPrice) {
        this.day = day;
        this.openingPrice = openingPrice;
        this.closingPrice = closingPrice;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
    }

    public void setClosingPrice(int closingPrice) {
        this.closingPrice = closingPrice;
    }
    public int getClosingPrice() {
        return this.closingPrice;
    }
    public int getOpeningPrice() {
        return this.openingPrice;
    }
    public int getMaxPrice() {
        return this.maxPrice;
    }
    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }
    public int getMinPrice() {
        return this.minPrice;
    }
    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }
}