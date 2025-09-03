package user;

import customExceptions.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class UserHandler {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

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

    public void updatePassword(String username, String oldPassword, String newPassword) throws SamePasswordException, InvalidPasswordException, UserNotFoundException {
        lock.lock();
        if (!userExists(username)) {
            lock.unlock();
            throw new UserNotFoundException(username);
        }
        if (isPasswordInvalid(oldPassword)) {
            lock.unlock();
            throw new InvalidPasswordException();
        }
        User user = users.get(username);
        user.updatePassword(oldPassword, newPassword);
        lock.unlock();
    }

    public User login(String username, String password) throws UserNotFoundException, UserLoggedInException, WrongPasswordException {
        lock.lock();
        if (!userExists(username)) {
            lock.unlock();
            throw new UserNotFoundException(username);
        }
        User user = users.get(username);
        user.login(password);
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
