package api.values;

public enum OrderDirection {
    BID,
    ASK;

    public static OrderDirection fromString(String s) {
        if (s.equalsIgnoreCase("BID")) {
            return BID;
        } else if (s.equalsIgnoreCase("ASK")) {
            return ASK;
        } else {
            throw new IllegalArgumentException("Invalid order direction: " + s);
        }
    }
}