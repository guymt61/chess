package server;

import com.google.gson.Gson;
import exception.ResponseException;
import requestsresults.*;
import spark.*;
import service.*;
import dataaccess.*;


public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public Server() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.delete("/db", this::clear);
        Spark.get("/game", this::list);
        Spark.post("/game", this::create);
        Spark.put("/game", this::join);
        Spark.exception(ResponseException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object clear(Request req, Response res) {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
        return "";
    }

    private Object login(Request req, Response res) throws ResponseException {
        LoginRequest logReq = new Gson().fromJson(req.body(), LoginRequest.class);
        LoginResult logRes = userService.login(logReq);
        return new Gson().toJson(logRes);
    }

    private Object register(Request req, Response res) throws ResponseException {
        RegisterRequest regReq = new Gson().fromJson(req.body(), RegisterRequest.class);
        RegisterResult regRes = userService.register(regReq);
        return new Gson().toJson(regRes);
    }

    private Object logout(Request req, Response res) throws ResponseException{
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("Authorization"));
        LogoutResult logoutResult = userService.logout(logoutRequest);
        return new Gson().toJson(logoutResult);
    }

    private Object list(Request req, Response res) throws ResponseException {
        ListRequest listRequest = new ListRequest(req.headers("Authorization"));
        ListResult listResult = gameService.list(listRequest);
        return new Gson().toJson(listResult);
    }

    private Object create(Request req, Response res) throws ResponseException {
        CreateRequest nameOnly = new Gson().fromJson(req.body(), CreateRequest.class);
        CreateRequest createRequest = new CreateRequest(req.headers("Authorization"), nameOnly.gameName());
        CreateResult createResult = gameService.create(createRequest);
        return new Gson().toJson(createResult);
    }

    private Object join(Request req, Response res) throws ResponseException, DataAccessException {
        JoinRequest withoutAuth = new Gson().fromJson(req.body(), JoinRequest.class);
        JoinRequest joinRequest = new JoinRequest(req.headers("Authorization"), withoutAuth.playerColor(), withoutAuth.gameID());
        JoinResult joinResult = gameService.join(joinRequest);
        return new Gson().toJson(joinResult);
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.body(ex.toJson());
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
