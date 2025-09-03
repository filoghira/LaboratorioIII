package api;

public class ResponseUser extends Response {
    private final int response;
    private final String errorMessage;

    public ResponseUser(int r, String e) {
        this.response = r;
        this.errorMessage = e;
    }

    public int getResponse() {
        return this.response;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
