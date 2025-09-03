package database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import order.Order;
import order.OrderHandler;
import user.User;
import user.UserHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;

public class DumbDatabase extends TimerTask {

    private final OrderHandler orderHandler;
    private final UserHandler userHandler;
    private int lastOrderId = 0;

    public DumbDatabase(OrderHandler orderHandler, UserHandler userHandler) {
        this.orderHandler = orderHandler;
        this.userHandler = userHandler;
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
                if (order.isDone()){
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
