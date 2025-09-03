package customExceptions;

public class WrongUserException extends CustomException{
    public WrongUserException(String username) {
        super(101, "You, " + username + ", are not allowed to perform this action.");
    }
}
