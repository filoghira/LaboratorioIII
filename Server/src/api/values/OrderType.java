package api.values;

import com.google.gson.annotations.SerializedName;

public enum OrderType {
    @SerializedName("market")
    MARKET,
    @SerializedName("limit")
    LIMIT,
    @SerializedName("stop")
    STOP
}
