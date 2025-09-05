import api.request.*;
import api.response.Response;
import api.response.ResponseOperation;
import api.response.ResponsePriceHistory;
import api.response.ResponseUser;
import api.values.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import customExceptions.*;
import order.OrderHandler;
import user.UserHandler;

import java.net.InetAddress;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class APIHandler {

    private static final Logger logger = Logger.getLogger(APIHandler.class.getName());

    /**
     * Handle an API request
     * @param thread Thread that received the request
     * @param request The request
     * @param userHandler User handler
     * @param orderHandler Order handler
     * @param ip IP address of the TCP connection
     * @return The response to the API request
     */
    public static Response HandleRequest(
            ClientThread thread,
            String request,
            UserHandler userHandler,
            OrderHandler orderHandler,
            InetAddress ip
    ) {
        // TypeAdapterFactory to handle the different subtypes of Request
        RuntimeTypeAdapterFactory<Operation> runtimeTypeAdapterFactory =
                RuntimeTypeAdapterFactory
                        .of(Operation.class, "operation", true)
                        .registerSubtype(PriceHistoryOperation.class, "getPriceHistory")
                        .registerSubtype(LimitOrderOperation.class, "insertLimitOrder")
                        .registerSubtype(MarketOrderOperation.class, "insertMarketOrder")
                        .registerSubtype(StopOrderOperation.class, "insertStopOrder")
                        .registerSubtype(CancelOrderOperation.class, "cancelOrder")
                        .registerSubtype(LogoutOperation.class, "logout")
                        .registerSubtype(Operation.class, "quit")
                        .registerSubtype(LoginOperation.class, "login")
                        .registerSubtype(UpdateCredentialsOperation.class, "updateCredentials")
                        .registerSubtype(RegisterOperation.class, "register");

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
                .setPrettyPrinting()
                .create();

        Operation oJson = gson.fromJson(request, Operation.class);
        Response response;

        // If the user is not logged in and the request can only be performed after that
        if (
                thread.getUser() == null
                && !oJson.getOperation().equals("login")
                && !oJson.getOperation().equals("register")
                && !oJson.getOperation().equals("updateCredentials")
                && !oJson.getOperation().equals("quit")
        ) {
            return new ResponseUser(Response.ERROR, "You must be logged in to perform this operation");
        }

        // Call the right function for each request and create the right subtype of response
        switch (oJson.getOperation()) {
            case "register": {
                RegisterOperation op = gson.fromJson(request, RegisterOperation.class);
                RegisterAndLoginValues values = op.getValues();
                try {
                    logger.log(Level.WARNING, "Registering user: " + values.getUsername() + " from IP: " + ip);
                    userHandler.register(values.getUsername(), values.getPassword());
                    response = new ResponseUser(Response.OK, "user.User registered successfully");
                } catch (UserAlreadyExistsException | InvalidPasswordException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "updateCredentials": {
                UpdateCredentialsOperation op = gson.fromJson(request, UpdateCredentialsOperation.class);
                UpdateCredentialsValues values = op.getValues();
                try {
                    userHandler.updatePassword(values.getUsername(), values.getOldPassword(), values.getNewPassword());
                    response = new ResponseUser(Response.OK, "Password updated successfully");
                } catch (SamePasswordException | UserNotFoundException | InvalidPasswordException |
                         UserLoggedInException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "login": {
                LoginOperation op = gson.fromJson(request, LoginOperation.class);
                RegisterAndLoginValues values = op.getValues();
                try {
                    // Set current user in the thread
                    thread.setUser(userHandler.login(values.getUsername(), values.getPassword(), ip));
                    response = new ResponseUser(Response.OK, "Welcome " + values.getUsername());
                } catch (UserNotFoundException | UserLoggedInException | WrongPasswordException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "logout": {
                try {
                    thread.currentUserLock.lock();
                    if (thread.getUser() == null) {
                        thread.currentUserLock.unlock();
                        throw new UserNotLoggedInException("null");
                    }
                    userHandler.logout(thread.getUser().getUsername());
                    thread.setUser(null);
                    thread.currentUserLock.unlock();
                    response = new ResponseUser(Response.OK, "Goodbye " + thread.getUser().getUsername());
                } catch (UserNotFoundException | UserNotLoggedInException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "insertLimitOrder": {
                LimitOrderOperation op = gson.fromJson(request, LimitOrderOperation.class);
                LimitAndStopOrderValues values = op.getValues();
                try {
                    int id = orderHandler.insertLimitOrder(
                            values.getType(),
                            values.getSize(),
                            values.getPrice(),
                            thread.getUser()
                    );
                    response = new ResponseOperation(id);
                } catch (IllegalOrderSizeException | IllegalOrderPriceException | OrderNotExecutableException e) {
                    response = new ResponseOperation(Response.ERROR);
                }
                break;
            }
            case "insertMarketOrder": {
                MarketOrderOperation op = gson.fromJson(request, MarketOrderOperation.class);
                MarketOrderValues values = op.getValues();
                try {
                    int id = orderHandler.insertMarketOrder(
                            values.getType(),
                            values.getSize(),
                            thread.getUser()
                    );
                    logger.log(Level.INFO, "Market order inserted with ID: " + id);
                    response = new ResponseOperation(id);
                } catch (IllegalOrderSizeException | OrderNotExecutableException e) {
                    response = new ResponseOperation(Response.ERROR);
                }
                break;
            }
            case "insertStopOrder": {
                StopOrderOperation op = gson.fromJson(request, StopOrderOperation.class);
                LimitAndStopOrderValues values = op.getValues();
                try {
                    int id = orderHandler.insertStopOrder(
                            values.getType(),
                            values.getSize(),
                            values.getPrice(),
                            thread.getUser()
                    );
                    response = new ResponseOperation(id);
                } catch (IllegalOrderSizeException | IllegalOrderPriceException | OrderNotExecutableException e) {
                    response = new ResponseOperation(Response.ERROR);
                }
                break;
            }
            case "cancelOrder": {
                CancelOrderOperation op = gson.fromJson(request, CancelOrderOperation.class);
                CancelOrderValues values = op.getValues();

                try {
                    orderHandler.cancelOrder(values.getOrderId(), thread.getUser());
                    response = new ResponseUser(
                            Response.OK,
                            "Order with ID " + values.getOrderId() + " cancelled successfully"
                    );
                } catch (WrongUserException | OrderAlreadyCompletedException | InvalidOrderException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "getPriceHistory": {
                PriceHistoryOperation op = gson.fromJson(request, PriceHistoryOperation.class);
                PriceHistoryValues values = op.getValues();

                YearMonth month = YearMonth.parse(values.getMonth(), DateTimeFormatter.ofPattern("MMyyyy"));
                response = new ResponsePriceHistory(
                        Response.OK,
                        "Price history retrieved successfully",
                        orderHandler.getPriceHistory(month, thread.getUser())
                );
                break;
            }
            case "quit":
                thread.currentUserLock.lock();
                if (thread.getUser() != null) {
                    try {
                        userHandler.logout(thread.getUser().getUsername());
                    } catch (UserNotFoundException | UserNotLoggedInException ignored) {
                        thread.currentUserLock.unlock();
                    }
                    thread.currentUserLock.unlock();
                }
                throw new QuitException();
            default:
                response = new ResponseUser(Response.NOT_HANDLED, "Error while parsing the request");
        }

        // Update the last active timestamp for the current user
        thread.currentUserLock.lock();
        if (thread.getUser() != null) {
            thread.getUser().setLastActive(new Date());
        }
        thread.currentUserLock.unlock();

        return response;
    }
}
