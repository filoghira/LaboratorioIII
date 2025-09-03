package customExceptions;

public class IllegalOrderSizeException extends CustomException {
    public IllegalOrderSizeException() {
        super(201, "order.Order size must be greater than zero.");
    }
}
