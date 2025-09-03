package server;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {

    private static final Logger logger = Logger.getLogger("server.log");

    public static void main(String[] args) {

        Properties prop = new Properties();
        final String CONFIG_FILE = "res/server.config";

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            prop.load(fis);
        } catch (FileNotFoundException e) {
            logger.error
            System.err.println("Configuration file not found: " + CONFIG_FILE);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + CONFIG_FILE);
            System.exit(1);
        }

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Integer.parseInt(prop.getProperty("port")));
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number in configuration file.");
            System.exit(1);
        } catch (IOException e) {
            // TODO: Use a logger
            e.printStackTrace();
            System.exit(1);
        }

        // TODO: Use a custom thread pool
        ExecutorService executorService = Executors.newCachedThreadPool();
        // TODO: Do I need to keep track of client threads?
        ArrayList<ClientThread> clientThreads = new ArrayList<>();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(clientSocket);
                executorService.submit(clientThread);
                clientThreads.add(clientThread);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
