import database.DumbDatabase;
import notification.NotificationHandler;
import order.OrderHandler;
import user.UserHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {

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
        NotificationHandler.port = Integer.parseInt(prop.getProperty("notification_port"));

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

            ExecutorService executorService = Executors.newCachedThreadPool();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientThread clientThread = new ClientThread(clientSocket, userHandler, orderHandler, Integer.parseInt(prop.getProperty("notification_port")));
                    executorService.submit(clientThread);
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
