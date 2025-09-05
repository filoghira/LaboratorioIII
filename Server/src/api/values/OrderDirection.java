package api.values;

import com.google.gson.annotations.SerializedName;

/**
 * Possible "type" of a trade
 */
public enum OrderDirection {
    @SerializedName("bid")
    BID,
    @SerializedName("ask")
    ASK;

    private final static String BID_VAL = "bid";
    private final static String ASK_VAL = "ask";

    /**
     * Convert a string to an element of the enum
     * @param s The string
     * @return The enum correspondent
     */
    public static OrderDirection fromString(String s) {
        if (s.equalsIgnoreCase(BID_VAL)) {
            return BID;
        } else if (s.equalsIgnoreCase(ASK_VAL)) {
            return ASK;
        } else {
            throw new IllegalArgumentException("Invalid order direction: " + s);
        }
    }
}