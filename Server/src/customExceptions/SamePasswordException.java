package customExceptions;

public class SamePasswordException extends CustomException{
    public SamePasswordException(){
        super(103, "New password cannot be the same as the old one");
    }
}
