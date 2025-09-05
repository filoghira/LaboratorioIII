package api.response;

import api.values.Day;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GSON class for a response to getPriceHistory
 */
public class ResponsePriceHistory extends Response{
    private final int code;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String message;
    private final HashMap<Integer, Day> days;

    public ResponsePriceHistory(int code, String message, HashMap<Integer, Day> days) {
        this.code = code;
        this.message = message;
        this.days = days;
    }

    public HashMap<Integer, Day> getDays() {
        return days;
    }

    public int getCode() {
        return code;
    }
}
