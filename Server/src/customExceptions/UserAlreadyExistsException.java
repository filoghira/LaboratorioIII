package customExceptions;

public class UserAlreadyExistsException extends CustomException{
    public UserAlreadyExistsException(String username) {
        super(102, "user.User '" + username + "' already exists");
    }
}
