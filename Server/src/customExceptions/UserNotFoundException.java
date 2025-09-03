package customExceptions;

public class UserNotFoundException extends CustomException{
    public UserNotFoundException(String user){
        super(102, "user.User " + user + " not found");
    }
}
