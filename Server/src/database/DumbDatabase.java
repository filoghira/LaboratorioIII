package database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import order.Order;
import order.OrderHandler;
import user.User;
import user.UserHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DumbDatabase extends TimerTask {

    private static final Logger logger = Logger.getLogger(DumbDatabase.class.getName());

    private final OrderHandler orderHandler;
    private final UserHandler userHandler;

    public DumbDatabase(OrderHandler orderHandler, UserHandler userHandler) {
        this.orderHandler = orderHandler;
        this.userHandler = userHandler;
    }

    public static ConcurrentHashMap<String, User> loadUsers(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
        try (BufferedReader fr = new BufferedReader(new FileReader("userData.json"))){
            User user;
            fr.readLine();
            fr.readLine();
            String line;
            while ((line = fr.readLine()) != null){
                if(line.charAt(0) == ',') line = line.substring(1);
                if(line.charAt(0) == ']') break;
                user = gson.fromJson(line, User.class);
                users.put(user.getUsername(), user);
            }
        } catch (IOException e) {
            return users;
        }
        return users;
    }

    public static ConcurrentHashMap<Integer, Order> loadOrders(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ConcurrentHashMap<Integer, Order> orders = new ConcurrentHashMap<>();
        try (BufferedReader fr = new BufferedReader(new FileReader("orderData.json"))){
            Order order;
            fr.readLine();
            fr.readLine();
            String line;
            while ((line = fr.readLine()) != null){
                if(line.charAt(0) == ',') line = line.substring(1);
                if(line.charAt(0) == ']') break;
                order = gson.fromJson(line, Order.class);
                order.execute();
                orders.put(order.getOrderId(), order);
            }
        } catch (IOException e) {
            return orders;
        }
        return orders;
    }

    @Override
    public void run() {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter fw = new FileWriter("userData.json")){
            fw.write("{\n\"users\": [\n");
            Iterator<User> iterator = userHandler.getUsers().values().iterator();
            while (iterator.hasNext()){
                User user = iterator.next();
                fw.write(gson.toJson(user).replace("\n", ""));
                if (iterator.hasNext()) {
                    fw.write("\n,");
                }
            }
            fw.write("\n]}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter fw = new FileWriter("orderData.json")){
            fw.write("{\n\"trades\": [\n");
            Iterator<Order> iterator = orderHandler.getOrders().values().iterator();
            while (iterator.hasNext()){
                Order order = iterator.next();
                if (order != null && order.isDone()){
                    fw.write(gson.toJson(order).replace("\n", ""));
                    if (iterator.hasNext())
                        fw.write("\n,");
                }
            }
            fw.write("\n]}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.log(Level.INFO, "Database loaded");
    }
}
