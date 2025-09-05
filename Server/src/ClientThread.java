import api.responses.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import customExceptions.QuitException;
import order.OrderHandler;
import user.User;
import user.UserHandler;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread implements Runnable{
    private static final Logger logger = Logger.getLogger(ClientThread.class.getName());
    private final Socket socket;
    private final UserHandler userHandler;
    private User user;
    private final OrderHandler orderHandler;
    private final int notificationPort;

    public void setUser(User user) {
        this.user = user;
    }

    public ClientThread(Socket socket, UserHandler userHandler, OrderHandler orderHandler, int notificationPort) {
        this.socket = socket;
        this.userHandler = userHandler;
        this.orderHandler = orderHandler;
        this.notificationPort = notificationPort;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Client thread started");

        BufferedReader in;
        PrintWriter out;
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

        RuntimeTypeAdapterFactory<Response> runtimeTypeAdapterFactory =
                RuntimeTypeAdapterFactory
                        .of(Response.class, "operation", true)
                        .registerSubtype(ResponseOperation.class, "operation")
                        .registerSubtype(ResponsePriceHistory.class, "priceHistory")
                        .registerSubtype(ResponseUser.class, "user");

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
                .setPrettyPrinting()
                .create();

        while (true) {
            String message = "";
            String line;
            try {
                line = in.readLine();
                while (!line.equals("}")) {
                    message += line;
                    line = in.readLine();
                }
                message += line;
                
                logger.log(Level.INFO, "Received message: " + message);
                HandleRequestReturnValue ret = APIHandler.HandleRequest(this, message, userHandler, user, orderHandler, socket.getInetAddress());
                if (ret.getResponse() != null) {
                    out.println(gson.toJson(ret.getResponse()));
                    logger.log(Level.INFO, "Message sent: " + gson.toJson(ret.getResponse()));

                } else {
                    logger.log(Level.WARNING, "No response to send");
                    out.println(gson.toJson("Something went wrong"));
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading message", e);
            } catch (QuitException e) {
                break;
            }
        }
    }
}
