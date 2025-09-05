package notification;

import api.responses.Notification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class NotificationHandler {

    public static int port;

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
