package customExceptions;

public class UserNotLoggedInException extends CustomException{
    public UserNotLoggedInException(String username) {
        super(101, "user.User " + username + " is not logged in.");
    }
}
