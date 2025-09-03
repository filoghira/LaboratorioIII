package notification;

import api.responses.Notification;
import order.Order;
import user.User;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationHandler {

    public NotificationHandler() {

    }

    public void sendNotification(ArrayList<Order> orders) {
        ConcurrentHashMap<String, Notification> notifications = new ConcurrentHashMap<>();

        for (Order order : orders) {
            if (notifications.containsKey(order.getUsername())){
                Notification n = new Notification();
                n.addTrade(order);
            }
        }
    }
}
