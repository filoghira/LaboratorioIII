package customExceptions;

public class InvalidOrderException extends CustomException{
    public InvalidOrderException() {
        super(101, "Invalid order parameters.");
    }
}
