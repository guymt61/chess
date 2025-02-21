package server;

import com.google.gson.Gson;
import requestsresults.*;
import spark.*;
import service.*;
import dataaccess.*;


public class Server {
    private final UserService userService;

    public Server() {
        UserDAO userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object register(Request req, Response res) {
        RegisterRequest regReq = new Gson().fromJson(req.body(), RegisterRequest.class);
        RegisterResult regRes = userService.register(regReq);
        return new Gson().toJson(regRes);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
