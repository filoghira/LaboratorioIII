package api.values;

/**
 * GSON class for the possible values of a login or register request
 */
public class RegisterAndLoginValues extends Values{
    private final String username;
    private final String password;

    public RegisterAndLoginValues(String u, String p) {
        this.username = u;
        this.password = p;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
