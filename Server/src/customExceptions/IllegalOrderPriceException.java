package customExceptions;

public class IllegalOrderPriceException extends CustomException {
    public IllegalOrderPriceException() {
        super(202, "order.Order price must be greater than zero.");
    }
}
