package user;

import customExceptions.*;
import database.DumbDatabase;

import java.net.InetAddress;
import java.util.Date;
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

    /**
     * Check if the password respects the requirements
     * @param password to be checked
     * @return TRUE if the password is valid, FALSE otherwise
     */
    private static boolean isPasswordInvalid(String password) {
        return password.length() < 8;
    }

    /**
     * Register a new user in the system
     * @param username of the user
     * @param password of the user
     * @throws UserAlreadyExistsException If a user with the same username already exists
     * @throws InvalidPasswordException If the password does not respect the requirements
     */
    public void register(String username, String password)
            throws UserAlreadyExistsException, InvalidPasswordException {
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

    /**
     * Update the password of a user
     * @param username of the user
     * @param oldPassword Old password
     * @param newPassword New password
     * @throws SamePasswordException The new password must be different from the old one
     * @throws InvalidPasswordException The password does not respect the requirements
     * @throws UserNotFoundException The user that requested the change does not exist
     * @throws UserLoggedInException A user must be logged out to perform this action
     */
    public void updatePassword(String username, String oldPassword, String newPassword)
            throws  SamePasswordException, InvalidPasswordException,
                    UserNotFoundException, UserLoggedInException {
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

    /**
     * Login
     * @param username of the user
     * @param password of the user
     * @param ip Current IP address of the TCP client
     * @return User object
     * @throws UserNotFoundException A user with this username does not exist
     * @throws UserLoggedInException The user is already logged in
     * @throws WrongPasswordException Wrong password
     */
    public User login(String username, String password, InetAddress ip)
            throws UserNotFoundException, UserLoggedInException, WrongPasswordException {
        lock.lock();
        if (!userExists(username)) {
            lock.unlock();
            throw new UserNotFoundException(username);
        }
        User user = users.get(username);
        user.login(password, ip);
        user.setLastActive(new Date());
        lock.unlock();
        return user;
    }

    /**
     * Logout
     * @param username of the user
     * @throws UserNotFoundException No user with that username exists
     * @throws UserNotLoggedInException The user is not logged in
     */
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
