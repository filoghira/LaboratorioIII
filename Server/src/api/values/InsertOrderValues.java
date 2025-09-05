package api.values;

/**
 * Parent class for the possible values of market operation
 */
public class InsertOrderValues extends Values {
    private final OrderDirection type;
    private final int size;

    public InsertOrderValues(OrderDirection t, int s) {
        this.type = t;
        this.size = s;
    }

    public OrderDirection getType() {
        return type;
    }
    public int getSize() {
        return size;
    }
}
