package customExceptions;

public class UserNotFoundException extends CustomException{
    public UserNotFoundException(String message){
        super(102, message);
    }
}
