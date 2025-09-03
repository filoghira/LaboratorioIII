package customExceptions;

public class UserNotLoggedIn extends RuntimeException {
    public UserNotLoggedIn(String message) {
        super(message);
    }
}
