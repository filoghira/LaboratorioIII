import user.User;
import user.UserHandler;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread implements Runnable{
    private static final Logger logger = Logger.getLogger(ClientThread.class.getName());
    private final Socket socket;
    private PrintWriter out;
    private final UserHandler userHandler;
    private User user;

    public ClientThread(Socket socket, UserHandler userHandler) {
        this.socket = socket;
        this.userHandler = userHandler;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Client thread started");

        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error getting socket streams", e);
            try {
                socket.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error closing socket", ex);
                return;
            }
            return;
        }

        while (true) {
            String message;
            try {
                message = in.readLine();
                APIHandler.HandleRequest(message, userHandler, user);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading message", e);
            }
        }
    }
}
