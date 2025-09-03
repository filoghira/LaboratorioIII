package user;

import customExceptions.SamePasswordException;
import customExceptions.UserLoggedInException;
import customExceptions.UserNotLoggedInException;
import customExceptions.WrongPasswordException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.util.Date;

public class User {
    private final String username;
    private String hashedPassword;
    private transient boolean loggedIn = false;
    private transient Date lastActive;

    public User(String username, String password) {
        this.username = username;
        this.hashedPassword = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode(password);
    }

    public void updatePassword(String oldPassword, String newPassword) throws SamePasswordException {
        if (!Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().matches(oldPassword, this.hashedPassword)) {
            throw new SamePasswordException();
        }

        hashedPassword = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode(newPassword);
    }

    public void login(String password) throws UserLoggedInException, WrongPasswordException {
        if (loggedIn) {
            throw new UserLoggedInException(username);
        }

        if (Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().matches(password, this.hashedPassword)) {
            loggedIn = true;
        } else {
            throw new WrongPasswordException();
        }
    }

    public void logout() throws UserNotLoggedInException {
        if (!loggedIn) {
            throw new UserNotLoggedInException(username);
        }
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }
}
