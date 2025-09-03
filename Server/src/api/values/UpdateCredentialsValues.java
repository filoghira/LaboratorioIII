package api;

public class UpdateCredentialsValues { //classe che contiene i valori per operazioni updateCredentials
    private final String username;
    private final String oldPassword;
    private final String newPassword;

    public UpdateCredentialsValues(String u, String op, String np) {
        this.username = u;
        this.oldPassword = op;
        this.newPassword = np;
    }

    public String getUsername() {
        return username;
    }
    public String getOldPassword() {
        return oldPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }
}
