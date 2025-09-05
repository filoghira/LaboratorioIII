package user;

import customExceptions.SamePasswordException;
import customExceptions.UserLoggedInException;
import customExceptions.UserNotLoggedInException;
import customExceptions.WrongPasswordException;

import java.net.InetAddress;
import java.util.Date;

public class User {
    private final String username;
    private String password;
    private transient boolean loggedIn = false;
    private transient Date lastActive;
    private transient InetAddress lastIP;

    public InetAddress getLastIP() {
        return lastIP;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Updates the password of the user
     * @param oldPassword of the user
     * @param newPassword of the user
     * @throws SamePasswordException The new password must be different from the old one
     */
    public void updatePassword(String oldPassword, String newPassword) throws SamePasswordException {
        if (oldPassword.equals(newPassword)) {
            throw new SamePasswordException();
        }

        password = newPassword;
    }

    /**
     * Login a user
     * @param password of the user
     * @param ip current IP address of the TCP client
     * @throws UserLoggedInException User already logged in
     * @throws WrongPasswordException Wrong password
     */
    public void login(String password, InetAddress ip) throws UserLoggedInException, WrongPasswordException {
        if (loggedIn) {
            throw new UserLoggedInException(username);
        }

        if (password.equals(this.password)) {
            loggedIn = true;
            lastIP = ip;
        } else {
            throw new WrongPasswordException();
        }
    }

    /**
     * Logout
     * @throws UserNotLoggedInException If the user is not logged in
     */
    public void logout() throws UserNotLoggedInException {
        if (!loggedIn) {
            throw new UserNotLoggedInException(username);
        }
        loggedIn = false;
        lastIP = null;
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
