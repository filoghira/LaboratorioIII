package api.responses;

import api.values.Day;

import java.util.concurrent.ConcurrentHashMap;

public class ResponsePriceHistory extends Response{
    private int code;
    private String message;
    private ConcurrentHashMap<Integer, Day> days = new ConcurrentHashMap<>();

    public ResponsePriceHistory(int code, String message, ConcurrentHashMap<Integer, Day> days) {
        this.code = code;
        this.message = message;
        this.days = days;
    }

    public Day getDay(int day) {
        return this.days.getOrDefault(day, null);
    }

    public void setDays(ConcurrentHashMap<Integer, Day> days) {
        this.days = days;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
