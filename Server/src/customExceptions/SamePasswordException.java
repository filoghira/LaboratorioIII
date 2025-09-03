package customExceptions;

public class SamePasswordException extends CustomException{
    public SamePasswordException(String message){
        super(103, message);
    }
}
