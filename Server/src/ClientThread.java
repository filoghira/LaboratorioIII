import api.response.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import customExceptions.QuitException;
import order.OrderHandler;
import user.User;
import user.UserHandler;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("DuplicatedCode")
public class ClientThread implements Runnable{
    private static final Logger logger = Logger.getLogger(ClientThread.class.getName());
    private final Socket socket;
    private final UserHandler userHandler;
    private User user;
    private final OrderHandler orderHandler;
    public final ReentrantLock currentUserLock = new ReentrantLock();

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public ClientThread(Socket socket, UserHandler userHandler, OrderHandler orderHandler) {
        this.socket = socket;
        this.userHandler = userHandler;
        this.orderHandler = orderHandler;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Client thread started");

        // Load the server configuration file
        Properties prop = new Properties();
        try {
            prop.load(ServerMain.class.getClassLoader().getResourceAsStream("server.config"));
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Configuration file not found.", e);
            System.exit(1);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading configuration file.", e);
            System.exit(1);
        }

        // Prepare the reader and the printer for the socket
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

        // Recurrent thread to check for inactivity
        Timer userTimeoutTimer = new Timer();
        UserTimeout userTimeout = new UserTimeout(
                this,
                Integer.parseInt(prop.getProperty("timeout"))
        );
        userTimeoutTimer.scheduleAtFixedRate(
                userTimeout,
                Integer.parseInt(prop.getProperty("timeout_delay")),
                Integer.parseInt(prop.getProperty("timeout_delay"))
        );

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

                currentUserLock.lock();
                if (user != null)
                    user.setLastActive(new Date());
                currentUserLock.unlock();
                logger.log(Level.INFO, "Last active updated");

                // Handle it
                Response ret = APIHandler.HandleRequest(
                        this,
                        message.toString(),
                        userHandler,
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
