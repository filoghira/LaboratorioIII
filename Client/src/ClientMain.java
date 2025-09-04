import api.requestOperations.Operation;
import api.responses.Response;
import api.responses.ResponseOperation;
import api.responses.ResponsePriceHistory;
import api.responses.ResponseUser;
import api.values.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientMain {
    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());

    public static void main(String[] args) {
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

        Socket socket = null;
        try {
            socket = new Socket(prop.getProperty("host"), Integer.parseInt(prop.getProperty("port")));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error connecting to server.", e);
            System.exit(1);
        }

        PrintWriter out;
        BufferedReader in;
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

            switch (words[0]) {
                case "register":
                    if (words.length < 3) {
                        System.out.println("Invalid command. Usage: register <username> <password>");
                        break;
                    }
                    RegisterAndLoginValues regValues = new RegisterAndLoginValues(words[1], words[2]);
                    operation = new api.requestOperations.RegisterOperation("register", regValues);
                    expectedResponse = "ResponseUser";
                    break;
                case "login":
                    if (words.length < 3) {
                        System.out.println("Invalid command. Usage: login <username> <password>");
                        break;
                    }
                    RegisterAndLoginValues loginValues = new RegisterAndLoginValues(words[1], words[2]);
                    operation = new api.requestOperations.LoginOperation("login", loginValues);
                    expectedResponse = "ResponseUser";
                    break;
                case "logout":
                    operation = new Operation("logout");
                    expectedResponse = "ResponseUser";
                    break;
                case "updateCredentials":
                    if (words.length < 4) {
                        System.out.println("Invalid command. Usage: updateCredentials <username> <old_password> <new_password>");
                        break;
                    }
                    UpdateCredentialsValues updateValues = new UpdateCredentialsValues(words[1], words[2], words[3]);
                    operation = new api.requestOperations.UpdateCredentialsOperation("updateCredentials", updateValues);
                    expectedResponse = "ResponseUser";
                    break;
                case "quit":
                    operation = new Operation("quit");
                    break;
                case "insertLimitOrder":
                    LimitAndStopOrderValues limitValues = new LimitAndStopOrderValues(OrderDirection.fromString(words[1]), Integer.parseInt(words[2]), Integer.parseInt(words[3]));
                    operation = new api.requestOperations.LimitOrderOperation("insertLimitOrder", limitValues);
                    expectedResponse = "ResponseOperation";
                    break;
                case "insertStopOrder":
                    LimitAndStopOrderValues stopValues = new LimitAndStopOrderValues(OrderDirection.fromString(words[1]), Integer.parseInt(words[2]), Integer.parseInt(words[3]));
                    operation = new api.requestOperations.LimitOrderOperation("insertStopOrder", stopValues);
                    expectedResponse = "ResponseOperation";
                    break;
                case "insertMarketOrder":
                    MarketOrderValues marketValues = new MarketOrderValues(OrderDirection.fromString(words[1]), Integer.parseInt(words[2]));
                    operation = new api.requestOperations.MarketOrderOperation("insertMarketOrder", marketValues);
                    expectedResponse = "ResponseOperation";
                    break;
                case "cancelOrder":
                    if (words.length < 2) {
                        System.out.println("Invalid command. Usage: cancelOrder <orderID>");
                        break;
                    }
                    CancelOrderValues cancelValues = new CancelOrderValues(Integer.parseInt(words[1]));
                    operation = new api.requestOperations.CancelOrderOperation("cancelOrder", cancelValues);
                    expectedResponse = "ResponseOperation";
                    break;
                case "getPriceHistory":
                    if (words.length < 2) {
                        System.out.println("Invalid command. Usage: getPriceHistory <month(MMYYYY)>");
                        break;
                    }
                    PriceHistoryValues priceValues = new PriceHistoryValues(words[1]);
                    operation = new api.requestOperations.PriceHistoryOperation("getPriceHistory", priceValues);
                    expectedResponse = "ResponsePriceHistory";
                    break;
                default:
                    System.out.println("Operation does not exist");
            }

            if (operation != null) {
                String jsonRequest = gson.toJson(operation);
                out.println(jsonRequest);

                logger.log(Level.INFO, "Request sent: " + jsonRequest);

                if (words[0].equals("quit")) {
                    break;
                }

                String jsonResponse = "";
                String line;
                try {
                    line = in.readLine();
                    while (!line.equals("}")) {
                        jsonResponse += line;
                        line = in.readLine();
                    }
                    jsonResponse += line;

                    logger.log(Level.INFO, "Response: " + jsonResponse);

                    if (expectedResponse.equals("ResponseUser")) {
                        ResponseUser res = gson.fromJson(jsonResponse, ResponseUser.class);
                        System.out.println("Code: " + res.getResponse() + " - Message: " + res.getErrorMessage());
                    } else if (expectedResponse.equals("ResponseOperation")) {
                        ResponseUser res = gson.fromJson(jsonResponse, ResponseUser.class);
                        if (res.getResponse() == Response.OK) {
                            System.out.println("Operation successful.");
                        } else {
                            System.out.println("Operation failed: " + res.getErrorMessage());
                        }
                    } else if (expectedResponse.equals("ResponsePriceHistory")) {
                        ResponsePriceHistory res = gson.fromJson(jsonResponse, ResponsePriceHistory.class);
                        System.out.println("Price History: ");
                        if (res.getDays().isEmpty()) {
                            System.out.println("No data available for the requested month.");
                        } else {
                            res.getDays().forEach((day, values) -> System.out.println("Day " + day + ": Open=" + values.getOpeningPrice() + ", Close=" + values.getClosingPrice() + ", High=" + values.getMaxPrice() + ", Low=" + values.getMinPrice()));
                        }

                    } else {
                        System.out.println("Unknown response type.");
                    }

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error reading response from server", e);
                    break;
                }
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing socket", e);
        }

    }
}
