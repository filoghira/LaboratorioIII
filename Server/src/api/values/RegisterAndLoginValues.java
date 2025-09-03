package api;

public class RegisterAndLoginValues { //classe che contiene i valori per operazioni register e login
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
