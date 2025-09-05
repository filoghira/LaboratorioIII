package user;

import api.responses.Notification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.corba.se.impl.activation.ServerMain;
import customExceptions.SamePasswordException;
import customExceptions.UserLoggedInException;
import customExceptions.UserNotLoggedInException;
import customExceptions.WrongPasswordException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public void updatePassword(String oldPassword, String newPassword) throws SamePasswordException {
        if (oldPassword.equals(newPassword)) {
            throw new SamePasswordException();
        }

        password = newPassword;
    }

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
