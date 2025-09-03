package user;

import customExceptions.UserNotFoundException;
import customExceptions.UserNotLoggedInException;

import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Logger;

public class UserTimeout extends TimerTask {

    Logger logger = Logger.getLogger(UserTimeout.class.getName());

    private final UserHandler userHandler;
    private final int TIMEOUT;

    public UserTimeout(UserHandler userHandler, int timeout) {
        this.userHandler = userHandler;
        this.TIMEOUT = timeout;
    }

    @Override
    public void run() {
        userHandler.getLock().lock();
        for (User user : userHandler.getUsers().values()) {
            if (user.isLoggedIn() && (new Date().getTime() - user.getLastActive().getTime()) > TIMEOUT) {
                try {
                    userHandler.logout(user.getUsername());
                } catch (UserNotLoggedInException e) {
                    logger.warning("User " + user.getUsername() + " was not logged in when trying to log out due to timeout.");
                } catch (UserNotFoundException e) {
                    logger.warning("User " + user.getUsername() + " not found when trying to log out due to timeout.");
                }
            }
        }
        userHandler.getLock().unlock();
    }
}
