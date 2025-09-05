import api.request.*;
import api.response.Response;
import api.response.ResponseOperation;
import api.response.ResponsePriceHistory;
import api.response.ResponseUser;
import api.values.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientMain {
    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());

    public static void main(String[] args) {
        // Load Client configuration file
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

        // Prepare the socket to connect to the server via TCP and via UDP for notifications
        Socket socket = null;
        DatagramSocket notificationSocket;
        NotificationReceiver notificationReceiver = null;
        try {
            socket = new Socket(prop.getProperty("host"), Integer.parseInt(prop.getProperty("port")));
            notificationSocket = new DatagramSocket(Integer.parseInt(prop.getProperty("notification_port")));
            notificationReceiver = new NotificationReceiver(notificationSocket);
            notificationReceiver.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error connecting to server.", e);
            System.exit(1);
        }

        // Prepare the reader and the printer for the socket
        PrintWriter out;
        BufferedReader in;
        //noinspection DuplicatedCode
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

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String input = "start";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Read keyboard input
        while (!input.equals("quit")) {
            try {
                input = stdIn.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            logger.log(Level.INFO, input);

            String[] words = input.split("\\W+");
            Operation operation = null;
            String expectedResponse = "";

            // Understand the command and create the right request
            switch (words[0]) {
                case "register":
                    if (words.length < 3) {
                        System.out.println("Invalid command. Usage: register <username> <password>");
                        break;
                    }
                    RegisterAndLoginValues regValues = new RegisterAndLoginValues(words[1], words[2]);
                    operation = new RegisterOperation("register", regValues);
                    expectedResponse = "ResponseUser";
                    break;
                case "login":
                    if (words.length < 3) {
                        System.out.println("Invalid command. Usage: login <username> <password>");
                        break;
                    }
                    RegisterAndLoginValues loginValues = new RegisterAndLoginValues(words[1], words[2]);
                    operation = new LoginOperation("login", loginValues);
                    expectedResponse = "ResponseUser";
                    break;
                case "logout":
                    operation = new Operation("logout");
                    expectedResponse = "ResponseUser";
                    break;
                case "updateCredentials":
                    if (words.length < 4) {
                        System.out.println(
                                "Invalid command. Usage: updateCredentials <username> <old_password> <new_password>"
                        );
                        break;
                    }
                    UpdateCredentialsValues updateValues = new UpdateCredentialsValues(words[1], words[2], words[3]);
                    operation = new UpdateCredentialsOperation("updateCredentials", updateValues);
                    expectedResponse = "ResponseUser";
                    break;
                case "quit":
                    operation = new Operation("quit");
                    break;
                case "insertLimitOrder":
                    LimitAndStopOrderValues limitValues = new LimitAndStopOrderValues(
                            OrderDirection.fromString(words[1]),
                            Integer.parseInt(words[2]),
                            Integer.parseInt(words[3])
                    );
                    operation = new LimitOrderOperation("insertLimitOrder", limitValues);
                    expectedResponse = "ResponseOperation";
                    break;
                case "insertStopOrder":
                    LimitAndStopOrderValues stopValues = new LimitAndStopOrderValues(
                            OrderDirection.fromString(words[1]),
                            Integer.parseInt(words[2]),
                            Integer.parseInt(words[3])
                    );
                    operation = new LimitOrderOperation("insertStopOrder", stopValues);
                    expectedResponse = "ResponseOperation";
                    break;
                case "insertMarketOrder":
                    MarketOrderValues marketValues = new MarketOrderValues(
                            OrderDirection.fromString(words[1]),
                            Integer.parseInt(words[2])
                    );
                    operation = new MarketOrderOperation("insertMarketOrder", marketValues);
                    expectedResponse = "ResponseOperation";
                    break;
                case "cancelOrder":
                    if (words.length < 2) {
                        System.out.println("Invalid command. Usage: cancelOrder <orderID>");
                        break;
                    }
                    CancelOrderValues cancelValues = new CancelOrderValues(Integer.parseInt(words[1]));
                    operation = new CancelOrderOperation("cancelOrder", cancelValues);
                    expectedResponse = "ResponseUser";
                    break;
                case "getPriceHistory":
                    if (words.length < 2) {
                        System.out.println("Invalid command. Usage: getPriceHistory <month(MMYYYY)>");
                        break;
                    }
                    PriceHistoryValues priceValues = new PriceHistoryValues(words[1]);
                    operation = new PriceHistoryOperation("getPriceHistory", priceValues);
                    expectedResponse = "ResponsePriceHistory";
                    break;
                default:
                    System.out.println("Operation does not exist");
            }

            if (operation != null) {
                // Send the request
                String jsonRequest = gson.toJson(operation);
                out.println(jsonRequest);

                logger.log(Level.INFO, "Request sent: " + jsonRequest);

                if (words[0].equals("quit")) {
                    break;
                }

                StringBuilder jsonResponse = new StringBuilder();
                String line;
                try {
                    // Wait for the response
                    line = in.readLine();
                    while (!line.equals("}")) {
                        jsonResponse.append(line);
                        line = in.readLine();
                    }
                    jsonResponse.append(line);

                    logger.log(Level.INFO, "Response: " + jsonResponse);

                    // Check the response based on sent request
                    switch (expectedResponse) {
                        case "ResponseUser": {
                            ResponseUser res = gson.fromJson(jsonResponse.toString(), ResponseUser.class);
                            if (res.getResponse() == Response.OK) {
                                System.out.println(res.getErrorMessage());
                            } else {
                                System.out.println(
                                        "Code: " + res.getResponse() +
                                        " - Message: " + res.getErrorMessage()
                                );
                            }
                            break;
                        }
                        case "ResponseOperation": {
                            ResponseOperation res = gson.fromJson(jsonResponse.toString(), ResponseOperation.class);
                            if (res.getOrderID() != -1) {
                                System.out.println("Operation successful. ID: " + res.getOrderID());
                            } else {
                                System.out.println("Operation failed.");
                            }
                            break;
                        }
                        case "ResponsePriceHistory": {
                            ResponsePriceHistory res = gson.fromJson(
                                    jsonResponse.toString(),
                                    ResponsePriceHistory.class
                            );

                            if (res.getCode() == Response.ERROR) {
                                System.out.println("Error retrieving price history.");
                            } else if (res.getDays() == null || res.getDays().isEmpty()) {
                                System.out.println("No data available for the requested month.");
                            } else {
                                System.out.println("Price History: ");
                                res.getDays().forEach(
                                        (day, values) ->
                                                System.out.println(
                                                        "Day " + day +
                                                        ": Open=" + values.getOpeningPrice() +
                                                        ", Close=" + values.getClosingPrice() +
                                                        ", High=" + values.getMaxPrice() +
                                                        ", Low=" + values.getMinPrice()
                                                )
                                );
                            }

                            break;
                        }
                        default:
                            System.out.println("Unknown response type.");
                            break;
                    }

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error reading response from server", e);
                    break;
                }
            }
        }

        try {
            socket.close();
            notificationReceiver.stopReceiver();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing socket", e);
        }

    }
}
