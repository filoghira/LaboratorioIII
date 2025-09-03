package customExceptions;

public class InvalidPasswordException extends CustomException{
    public InvalidPasswordException(){
        super(101, "Invalid password");
    }
}
