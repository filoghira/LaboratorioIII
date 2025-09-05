package notification;

import api.response.Notification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NotificationHandler {

    public static int port;

    /**
     * Send a Notification to an address via UDP
     * @param notification The notification to be sent
     * @param address Server address
     */
    public static void sendNotification(Notification notification, InetAddress address) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        byte[] data = gson.toJson(notification).getBytes();

        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    address,
                    port
            );
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
