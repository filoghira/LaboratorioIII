import api.requestOperations.*;
import api.responses.Response;
import api.responses.ResponseOperation;
import api.responses.ResponsePriceHistory;
import api.responses.ResponseUser;
import api.values.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import customExceptions.*;
import order.OrderHandler;
import user.User;
import user.UserHandler;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class APIHandler {

    private static Logger logger = Logger.getLogger(APIHandler.class.getName());

    public static Response HandleRequest(String request, UserHandler userHandler, User currentUser, OrderHandler orderHandler, String ip) {
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
        Response response = null;

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
                } catch (SamePasswordException | UserNotFoundException |
                         InvalidPasswordException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "login": {
                LoginOperation op = gson.fromJson(request, LoginOperation.class);
                RegisterAndLoginValues values = op.getValues();
                try {
                    currentUser = userHandler.login(values.getUsername(), values.getPassword(), ip);
                    response = new ResponseUser(Response.OK, "user.User logged in successfully");
                } catch (UserNotFoundException | UserLoggedInException | WrongPasswordException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "logout": {
                try {
                    userHandler.logout(currentUser.getUsername());
                    currentUser = null;
                } catch (UserNotFoundException | UserNotLoggedInException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "insertLimitOrder": {
                LimitOrderOperation op = gson.fromJson(request, LimitOrderOperation.class);
                LimitAndStopOrderValues values = op.getValues();
                try {
                    int id = orderHandler.insertLimitOrder(values.getType(), values.getSize(), values.getPrice(), currentUser);
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
                    int id = orderHandler.insertMarketOrder(values.getType(), values.getSize(), currentUser);
                    response = new ResponseOperation(id);
                } catch (IllegalOrderSizeException e) {
                    response = new ResponseOperation(Response.ERROR);
                }
                break;
            }
            case "insertStopOrder": {
                StopOrderOperation op = gson.fromJson(request, StopOrderOperation.class);
                LimitAndStopOrderValues values = op.getValues();
                try {
                    int id = orderHandler.insertStopOrder(values.getType(), values.getSize(), values.getPrice(), currentUser);
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
                    orderHandler.cancelOrder(values.getOrderId(), currentUser);
                    response = new ResponseUser(Response.OK, "order.Order cancelled successfully");
                } catch (WrongUserException | OrderAlreadyCompletedException e) {
                    response = new ResponseUser(e.getCode(), e.getMessage());
                }
                break;
            }
            case "getPriceHistory": {
                PriceHistoryOperation op = gson.fromJson(request, PriceHistoryOperation.class);
                PriceHistoryValues values = op.getValues();

                YearMonth month = YearMonth.parse(values.getMonth(), DateTimeFormatter.ofPattern("MMyyyy"));
                response = new ResponsePriceHistory(Response.OK, "Price history retrieved successfully", orderHandler.getPriceHistory(month, currentUser));
                break;
            }
            case "quit":
                throw new QuitException();
            default:
                response = new ResponseUser(Response.NOT_HANDLED, "Error while parsing the request");
        }

        if (currentUser != null) {
            currentUser.setLastActive(new Date());
        }

        return response;
    }
}
