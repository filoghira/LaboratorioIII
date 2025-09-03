package api.responses;

public class ResponseUser extends Response {
    private final int response;
    private final String errorMessage;

    public ResponseUser(int errorCode, String errorMessage) {
        this.response = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getResponse() {
        return this.response;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
