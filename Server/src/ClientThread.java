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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread implements Runnable{
    private static final Logger logger = Logger.getLogger(ClientThread.class.getName());
    private final Socket socket;
    private final UserHandler userHandler;
    private User user;
    private final OrderHandler orderHandler;

    public void setUser(User user) {
        this.user = user;
    }

    public ClientThread(Socket socket, UserHandler userHandler, OrderHandler orderHandler) {
        this.socket = socket;
        this.userHandler = userHandler;
        this.orderHandler = orderHandler;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Client thread started");

        // Prepare the reader and the printer for the socket
        BufferedReader in;
        PrintWriter out;
        //noinspection DuplicatedCode
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

        // TypeAdapterFactory to handle the different subtypes of Response
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
            StringBuilder message = new StringBuilder();
            String line;
            try {
                // Receive a message
                line = in.readLine();
                while (!line.equals("}")) {
                    message.append(line);
                    line = in.readLine();
                }
                message.append(line);
                logger.log(Level.INFO, "Received message: " + message);

                // Handle it
                Response ret = APIHandler.HandleRequest(
                        this,
                        message.toString(),
                        userHandler,
                        user,
                        orderHandler,
                        socket.getInetAddress()
                );

                // Answer
                out.println(gson.toJson(ret));
                logger.log(Level.INFO, "Message sent: " + gson.toJson(ret));

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading message", e);
            } catch (QuitException e) {
                break;
            }
        }
    }
}
