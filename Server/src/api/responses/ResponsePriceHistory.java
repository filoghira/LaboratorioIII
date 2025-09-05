package api.responses;

import api.values.Day;

import java.util.concurrent.ConcurrentHashMap;

/**
 * GSON class for a response to getPriceHistory
 */
public class ResponsePriceHistory extends Response{
    private final int code;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String message;
    private final ConcurrentHashMap<Integer, Day> days;

    public ResponsePriceHistory(int code, String message, ConcurrentHashMap<Integer, Day> days) {
        this.code = code;
        this.message = message;
        this.days = days;
    }

    public ConcurrentHashMap<Integer, Day> getDays() {
        return days;
    }

    public int getCode() {
        return code;
    }
}
