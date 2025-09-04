package database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import order.Order;
import order.OrderHandler;
import user.User;
import user.UserHandler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class DumbDatabase extends TimerTask {

    private final OrderHandler orderHandler;
    private final UserHandler userHandler;
    private int lastOrderId = 0;

    public DumbDatabase(OrderHandler orderHandler, UserHandler userHandler) {
        this.orderHandler = orderHandler;
        this.userHandler = userHandler;
    }

    public static ConcurrentHashMap<String, User> loadUsers(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
        try (FileReader fr = new FileReader("userData.json")){
            User user;
            while (fr.ready()){
                user = gson.fromJson(fr, User.class);
                users.put(user.getUsername(), user);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public static ConcurrentHashMap<Integer, Order> loadOrders(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ConcurrentHashMap<Integer, Order> orders = new ConcurrentHashMap<>();
        try (FileReader fr = new FileReader("orderData.json")){
            Order order;
            while (fr.ready()){
                order = gson.fromJson(fr, Order.class);
                orders.put(order.getOrderID(), order);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return orders;
    }

    @Override
    public void run() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter fw = new FileWriter("userData.json")){
            for (User user : userHandler.getUsers().values())
                fw.write(gson.toJson(user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter fw = new FileWriter("orderData.json")){
            for (int i = lastOrderId; i < orderHandler.getOrders().size(); i++) {
                Order order = orderHandler.getOrders().get(i);
                if (order != null && order.isDone()){
                    try {
                        fw.write(gson.toJson(order));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    lastOrderId++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
