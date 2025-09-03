package customExceptions;

public class IllegalOrderPriceException extends RuntimeException {
  public IllegalOrderPriceException(String message) {
    super(message);
  }
}
