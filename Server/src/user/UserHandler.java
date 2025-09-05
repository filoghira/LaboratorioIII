package user;

import customExceptions.*;
import database.DumbDatabase;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class UserHandler {
    private final ConcurrentHashMap<String, User> users;
    private final ReentrantLock lock = new ReentrantLock();

    public UserHandler() {
        users = DumbDatabase.loadUsers();
    }

    public ReentrantLock getLock() {
        return lock;
    }

    private boolean userExists(String username) {
        return users.containsKey(username);
    }

    private static boolean isPasswordInvalid(String password) {
        return password.length() < 8;
    }

    public void register(String username, String password) throws UserAlreadyExistsException, InvalidPasswordException {
        lock.lock();
        if (userExists(username)) {
            lock.unlock();
            throw new UserAlreadyExistsException(username);
        }

        if (isPasswordInvalid(password)) {
            lock.unlock();
            throw new InvalidPasswordException();
        }

        User user = new User(username, password);
        users.put(username, user);
        lock.unlock();
    }

    public void updatePassword(String username, String oldPassword, String newPassword) throws SamePasswordException, InvalidPasswordException, UserNotFoundException, UserLoggedInException {
        lock.lock();
        if (users.get(username).isLoggedIn()){
            lock.unlock();
            throw new UserLoggedInException(username);
        }
        if (!userExists(username)) {
            lock.unlock();
            throw new UserNotFoundException(username);
        }
        if (isPasswordInvalid(newPassword)) {
            lock.unlock();
            throw new InvalidPasswordException();
        }
        User user = users.get(username);
        user.updatePassword(oldPassword, newPassword);
        lock.unlock();
    }

    public User login(String username, String password, InetAddress ip) throws UserNotFoundException, UserLoggedInException, WrongPasswordException {
        lock.lock();
        if (!userExists(username)) {
            lock.unlock();
            throw new UserNotFoundException(username);
        }
        User user = users.get(username);
        user.login(password, ip);
        lock.unlock();
        return user;
    }

    public void logout(String username) throws UserNotFoundException, UserNotLoggedInException {
        lock.lock();
        if (!userExists(username)) {
            lock.unlock();
            throw new UserNotFoundException(username);
        }
        User user = users.get(username);
        user.logout();
        lock.unlock();
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }
}
