package customExceptions;

public class UserLoggedInException extends CustomException{
    public UserLoggedInException(String user){
        super(104, "user.User " + user + " is already logged in");
    }
}
