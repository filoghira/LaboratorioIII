package api;

public class LimitAndStopOrderValues extends InsertOrderValues{
    private final int price;

    public LimitAndStopOrderValues(String t, int s, int p) {
        super(t, s);
        this.price = p;
    }
    public int getPrice() {
        return price;
    }
}
