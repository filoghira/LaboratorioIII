package customExceptions;

public class UserAlreadyExistsException extends CustomException{
    public UserAlreadyExistsException(String message){
        super(102, message);
    }
}
