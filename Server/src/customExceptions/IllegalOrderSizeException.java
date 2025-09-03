package customExceptions;

public class IllegalOrderSizeException extends RuntimeException {
    public IllegalOrderSizeException(String message) {
        super(message);
    }
}
