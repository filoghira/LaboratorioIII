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

    /**
     * Retrieve all the users saved in the "database" file
     * @return HashMap of each user mapped to its username
     */
    public static ConcurrentHashMap<String, User> loadUsers(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

        try (BufferedReader fr = new BufferedReader(new FileReader("userData.json"))){
            User user;

            // Skip the first two lines (no data)
            fr.readLine();
            fr.readLine();

            String line;
            while ((line = fr.readLine()) != null){
                if(!line.isEmpty()) {
                    // Remove the comma if present
                    if (line.charAt(0) == ',') line = line.substring(1);
                    // End of JSON list reached
                    if (line.charAt(0) == ']') break;

                    user = gson.fromJson(line, User.class);
                    users.put(user.getUsername(), user);
                }
            }
        } catch (IOException e) {
            return users;
        }
        return users;
    }

    /**
     * Retrieve all the orders saved in the "database" file
     * @return a LoadOrdersReturnValue, composed of:
     * - a HashMap of the Order objects mapped to their orderId
     * - the value of the highest ID
     */
    public static LoadOrdersReturnValue loadOrders(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ConcurrentHashMap<Integer, Order> orders = new ConcurrentHashMap<>();

        int maxId = 0;

        try (BufferedReader fr = new BufferedReader(new FileReader("orderData.json"))){
            Order order;

            // Skip the first two lines (no data)
            fr.readLine();
            fr.readLine();

            String line;
            while ((line = fr.readLine()) != null){
                if(!line.isEmpty()) {
                    // Remove comma if present
                    if (line.charAt(0) == ',') line = line.substring(1);
                    // End of JSON list reached
                    if (line.charAt(0) == ']') break;

                    order = gson.fromJson(line, Order.class);

                    // Since it was saved, it has been executed
                    order.execute();

                    orders.put(order.getOrderId(), order);
                    if (order.getOrderId() > maxId) maxId = order.getOrderId();
                }
            }
        } catch (IOException e) {
            return new LoadOrdersReturnValue(maxId, orders);
        }
        return new LoadOrdersReturnValue(maxId, orders);
    }

    @Override
    public void run() {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Persist the User data on the "database" file
        try (FileWriter fw = new FileWriter("userData.json")){
            // JSON preamble
            fw.write("{\n\"users\": [\n");

            Iterator<User> iterator = userHandler.getUsers().values().iterator();
            while (iterator.hasNext()){
                User user = iterator.next();
                fw.write(gson.toJson(user).replace("\n", ""));

                // Add comma until the last
                if (iterator.hasNext()) fw.write("\n,");
            }
            // JSON postamble
            fw.write("\n]}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Persist the Order data on the "database" file
        try (FileWriter fw = new FileWriter("orderData.json")){
            // JSON preamble
            fw.write("{\n\"trades\": [\n");

            Iterator<Order> iterator = orderHandler.getOrders().values().iterator();
            while (iterator.hasNext()){
                Order order = iterator.next();
                if (order != null && order.isDone()){
                    fw.write(gson.toJson(order).replace("\n", ""));

                    // Add comma until the last
                    if (iterator.hasNext()) fw.write("\n,");
                }
            }
            // JSON postamble
            fw.write("\n]}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.log(Level.INFO, "Database saved");
    }
}
