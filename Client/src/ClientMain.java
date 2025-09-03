import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientMain {
    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());

    public static void main(String[] args) {
        Properties prop = new Properties();
        try {
            prop.load(ClientMain.class.getClassLoader().getResourceAsStream("client.config"));
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Configuration file not found.", e);
            System.exit(1);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading configuration file.", e);
            System.exit(1);
        }

        Socket socket = null;
        try {
            socket = new Socket(prop.getProperty("host"), Integer.parseInt(prop.getProperty("port")));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error connecting to server.", e);
            System.exit(1);
        }

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

        String message = "{\"type\":\"greeting\",\"content\":\"Hello, Server!\"}\n";
        out.println(message);
        String response = null;
        try {
            response = in.readLine();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading message", e);
        }
        System.out.println("Received from server: " + response);

        try {
            socket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing socket", e);
        }

    }
}
