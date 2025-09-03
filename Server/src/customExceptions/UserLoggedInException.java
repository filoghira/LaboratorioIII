package customExceptions;

public class UserLoggedInException extends CustomException{
    public UserLoggedInException(String message){
        super(104, message);
    }
}
