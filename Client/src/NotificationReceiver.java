import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationReceiver extends Thread {
    private final DatagramSocket socket;
    private boolean running = true;
    private static final Logger logger = Logger.getLogger(NotificationReceiver.class.getName());

    public NotificationReceiver(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Notification: " + message);
            }
        } catch (IOException e) {
            if (socket.isClosed()) {
                logger.log(Level.WARNING, "Socket closed, stopping notification receiver.");
            } else {
                logger.log(Level.WARNING, "Error receiving notification from server.");
            }
        }
    }

    public void stopReceiver() {
        running = false;
        socket.close();
    }
}
