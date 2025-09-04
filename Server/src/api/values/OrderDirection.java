package api.values;

import com.google.gson.annotations.SerializedName;

public enum OrderDirection {
    @SerializedName("bid")
    BID,
    @SerializedName("ask")
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