import database.DumbDatabase;
import order.OrderHandler;
import user.UserHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {

    // TODO: Save logs to a file
    private static final Logger logger = Logger.getLogger(ServerMain.class.getName());

    public static void main(String[] args) {
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

        UserHandler userHandler = new UserHandler();
        OrderHandler orderHandler = new OrderHandler();

        Timer dbTimer = new Timer();
        DumbDatabase db = new DumbDatabase(orderHandler, userHandler);
        dbTimer.scheduleAtFixedRate(db, Integer.parseInt(prop.getProperty("delay")), Integer.parseInt(prop.getProperty("delay")));

        Timer userTimeoutTimer = new Timer();
        user.UserTimeout userTimeout = new user.UserTimeout(userHandler, Integer.parseInt(prop.getProperty("timeout")));
        userTimeoutTimer.scheduleAtFixedRate(userTimeout, Integer.parseInt(prop.getProperty("timeout_delay")), Integer.parseInt(prop.getProperty("timeout_delay")));

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(prop.getProperty("port")))){
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    serverSocket.close();
                    db.run();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            // TODO: Use a custom thread pool
            ExecutorService executorService = Executors.newCachedThreadPool();
            // TODO: Do I need to keep track of client threads?
            ArrayList<ClientThread> clientThreads = new ArrayList<>();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientThread clientThread = new ClientThread(clientSocket, userHandler, orderHandler);
                    executorService.submit(clientThread);
                    clientThreads.add(clientThread);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error accepting client connection.", e);
                }
            }
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Invalid port number.", e);
            System.exit(1);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error opening server socket.", e);
            System.exit(1);
        }
    }
}
