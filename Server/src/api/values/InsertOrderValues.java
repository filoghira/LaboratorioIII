package api;

public class InsertOrderValues {
    private final String type;
    private final int size;

    public InsertOrderValues(String t, int s) {
        this.type = t;
        this.size = s;
    }

    public String getType() {
        return type;
    }
    public int getSize() {
        return size;
    }
}
