package user;

import customExceptions.*;

import java.util.concurrent.ConcurrentHashMap;

public class UserHandler {
    private ConcurrentHashMap<String, User> users;

    private boolean userExists(String username) {
        return users.containsKey(username);
    }

    private static boolean isPasswordInvalid(String password) {
        return password.length() < 8;
    }

    public void register(String username, String password) throws UserAlreadyExistsException, InvalidPasswordException {
        if (userExists(username)) {
            throw new UserAlreadyExistsException(username);
        }

        if (isPasswordInvalid(password)) {
            throw new InvalidPasswordException();
        }

        User user = new User(username, password);
        users.put(username, user);
    }

    public void updatePassword(String username, String oldPassword, String newPassword) throws SamePasswordException, InvalidPasswordException, UserNotFoundException {
        if (!userExists(username)) {
            throw new UserNotFoundException(username);
        }
        if (isPasswordInvalid(oldPassword)) {
            throw new InvalidPasswordException();
        }
        User user = users.get(username);
        user.updatePassword(oldPassword, newPassword);
    }

    public User login(String username, String password) throws UserNotFoundException, UserLoggedInException, WrongPasswordException {
        if (!userExists(username)) {
            throw new UserNotFoundException(username);
        }
        User user = users.get(username);
        user.login(password);
        return user;
    }

    public void logout(String username) throws UserNotFoundException, UserNotLoggedInException {
        if (!userExists(username)) {
            throw new UserNotFoundException(username);
        }
        User user = users.get(username);
        user.logout();
    }

    public boolean isLoggedIn(String username) throws UserNotFoundException {
        if (!userExists(username)) {
            throw new UserNotFoundException(username);
        }
        User user = users.get(username);
        return user.isLoggedIn();
    }
}
