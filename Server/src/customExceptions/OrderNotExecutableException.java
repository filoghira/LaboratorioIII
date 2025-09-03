package customExceptions;

public class OrderNotExecutableException extends CustomException{
    public OrderNotExecutableException() {
        super(-1, "order.Order not executable");
    }
}
