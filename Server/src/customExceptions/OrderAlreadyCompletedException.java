package customExceptions;

public class OrderAlreadyCompletedException extends CustomException{
    public OrderAlreadyCompletedException(int orderID) {
        super(101, "order.Order with ID "+ orderID +" is already completed.");
    }
}
