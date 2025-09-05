import customExceptions.UserNotLoggedInException;

import java.util.Date;
import java.util.TimerTask;

public class UserTimeout extends TimerTask {

    private final ClientThread thread;
    private final int TIMEOUT;

    public UserTimeout(ClientThread thread, int timeout) {
        this.thread = thread;
        this.TIMEOUT = timeout;
    }

    @Override
    public void run() {
        thread.currentUserLock.lock();
        if(thread.getUser() != null) {
            if (thread.getUser().getLastActive().getTime() - new Date().getTime() < TIMEOUT) {
                try {
                    thread.getUser().logout();
                } catch (UserNotLoggedInException e) {
                    thread.currentUserLock.unlock();
                    throw new RuntimeException(e);
                }
                thread.setUser(null);
            }
        }
        thread.currentUserLock.unlock();
    }
}
