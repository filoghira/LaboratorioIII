package customExceptions;

public class WrongPasswordException extends CustomException{
    public WrongPasswordException(String message){
        super(102, message);
    }
}
