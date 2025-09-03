package customExceptions;

public class WrongPasswordException extends CustomException{
    public WrongPasswordException() {
        super(102, "Wrong password");
    }
}
