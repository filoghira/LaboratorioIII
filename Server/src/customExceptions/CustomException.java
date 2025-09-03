package customExceptions;

public class CustomException extends Exception{
    private final int code;

    public CustomException(int code, String message){
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}