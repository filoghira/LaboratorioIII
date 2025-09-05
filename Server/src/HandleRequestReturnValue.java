import api.responses.Response;
import user.User;

public class HandleRequestReturnValue {
    private final Response response;
    private final User user;

    public HandleRequestReturnValue(Response response, User user) {
        this.response = response;
        this.user = user;
    }

    public Response getResponse() {
        return response;
    }

    public User getUser() {
        return user;
    }
}
