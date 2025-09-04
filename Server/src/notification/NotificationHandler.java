package notification;

import api.responses.Notification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import order.Order;
import user.User;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class NotificationHandler {
    public static int port;

    public static void sendNotification(ArrayList<User> users, ArrayList<Order> orders) {
        for (User user : users) {
            Notification n = new Notification(orders);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(n);

            byte[] bytes = json.getBytes();
            try {
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(user.getLastIP()), port);
                DatagramSocket socket = new DatagramSocket(port);
                socket.send(packet);
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
