package api.values;

import com.google.gson.annotations.SerializedName;

/**
 * Possible "orderType" of a trade
 */
public enum OrderType {
    @SerializedName("market")
    MARKET,
    @SerializedName("limit")
    LIMIT,
    @SerializedName("stop")
    STOP
}
