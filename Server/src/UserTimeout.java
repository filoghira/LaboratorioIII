import customExceptions.UserNotLoggedInException;

import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserTimeout extends TimerTask {

    private final ClientThread thread;
    private final int TIMEOUT;
    private static final Logger logger = Logger.getLogger(UserTimeout.class.getName());

    public UserTimeout(ClientThread thread, int timeout) {
        this.thread = thread;
        this.TIMEOUT = timeout;
    }

    @Override
    public void run() {
        thread.currentUserLock.lock();
        if(thread.getUser() != null) {
            if (new Date().getTime() - thread.getUser().getLastActive().getTime() >= TIMEOUT) {
                try {
                    thread.getUser().logout();
                } catch (UserNotLoggedInException e) {
                    thread.currentUserLock.unlock();
                    throw new RuntimeException(e);
                }
                logger.log(Level.INFO, "User " + thread.getUser().getUsername() + " logged out due to inactivity.");
                thread.setUser(null);
            }
        }
        thread.currentUserLock.unlock();
    }
}
