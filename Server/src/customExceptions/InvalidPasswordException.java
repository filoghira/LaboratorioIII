package customExceptions;

public class InvalidPasswordException extends CustomException{
    public InvalidPasswordException(String message){
        super(101, message);
    }
}
