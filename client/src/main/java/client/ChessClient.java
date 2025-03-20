package client;
import chess.ChessGame;
import exception.ResponseException;
import model.*;
import requestsresults.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static ui.EscapeSequences.SET_TEXT_COLOR_RED;

public class ChessClient {

    private State state;
    private final ServerFacade server;
    private String authToken;
    private String username;
    private HashMap<Integer, Integer> displayedIDConverter;
    private ChessGame activeGame;
    private String activeGameName;
    private ChessGame.TeamColor pov;
    private ChessboardDrawer drawer;

    public ChessClient(String serverurl) {
        server = new ServerFacade(serverurl);
        state = State.LOGGEDOUT;
    }

    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd.toLowerCase()) {
                case "login" -> logIn(params);
                case "register" -> register(params);
                case "logout" -> logOut();
                case "create" -> create(params);
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "quit" -> "Thank you for using the chess client!";
                default -> help();
            };
        } catch (ResponseException ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage();
        }
    }

    public String help() {
        if (state == State.LOGGEDOUT) {
            return """
                    logIn <username> <password> - log in to play as an existing user
                    register <username> <password> <email> - create a new user to play chess
                    help - list commands
                    quit - exit the chess client
                    """;
        }
        if (state == State.LOGGEDIN) {
            return """
                    logOut - log out of this user
                    create <name> - create a new chess game
                    list - list all active chess games
                    join <id> [WHITE|BLACK] - join a chess game
                    observe <id> - watch a game without playing
                    help - list possible commands
                    """;
        }
        return null;
    }

    public String logIn(String... params) throws ResponseException {
        if (params.length >= 2) {
            LoginResult result = server.login(params[0], params[1]);
            if (result != null) {
                authToken = result.authToken();
                username = result.username();
                state = State.LOGGEDIN;
                return String.format("Logged in as %s", username);
            }
            else {
                throw new ResponseException(407, "Empty auth retrieved");
            }
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            String newUsername = params[0];
            String password = params[1];
            String email = params[2];
            RegisterResult result = server.register(newUsername, password, email);
            if (result != null) {
                authToken = result.authToken();
                username = newUsername;
                state = State.LOGGEDIN;
                return String.format("Registered and logged in as %s", username);
            }
            else {
                throw new ResponseException(407, "Register failed");
            }
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String logOut() throws ResponseException {
        assertLoggedIn();
        server.logout(authToken);
        username = null;
        authToken = null;
        state = State.LOGGEDOUT;
        return "You have been logged out.";
    }

    public String create(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length >= 1) {
            String gameName = params[0];
            server.create(gameName, authToken);
            return String.format("New chess game created named '%s'", gameName);
        }
        throw new ResponseException(407, "Expected: <name>");
    }

    public String list() throws ResponseException {
        assertLoggedIn();
        String output = "";
        ListResult listResult = server.list(authToken);
        ArrayList<GameData> allGames = listResult.games();
        displayedIDConverter = new HashMap<>();
        for (int i = 0; i < allGames.size(); i++) {
            GameData game = allGames.get(i);
            displayedIDConverter.put(i+1, game.gameID());
            String white;
            String black;
            if (game.whiteUsername() == null) {
                white = "no one";
            }
            else {
                white = game.whiteUsername();
            }
            if (game.blackUsername() == null) {
                black = "no one";
            }
            else {
                black = game.blackUsername();
            }
            String rawString = "%d: '%s' with white controlled by %s and black controlled by %s.%n";
            String formatted = String.format(rawString, i+1, game.gameName(), white, black);
            output += formatted;
        }
        return output;
    }

    public String join(String... params) throws ResponseException {
        assertLoggedIn();
        if (displayedIDConverter.isEmpty()) {
            throw new ResponseException(411, "Error: There are either no active games to join, or you have not listed games");
        }
        if (params.length >= 2) {
            int displayedID = Integer.parseInt(params[0]);
            int trueID = displayedIDConverter.get(displayedID);
            String color = params[1].toUpperCase();
            server.join(color, trueID, authToken);
            state = State.INGAME;
            if (color.equals("BLACK")) {
                pov = ChessGame.TeamColor.BLACK;
            }
            else {
                pov = ChessGame.TeamColor.WHITE;
            }
            ListResult listResult = server.list(authToken);
            for (GameData data : listResult.games()) {
                if (data.gameID() == trueID) {
                    activeGame = data.game();
                    activeGameName = data.gameName();
                    break;
                }
            }
            String rawString = "Successfully joined game %s controlling %s.%n";
            String formatted = String.format(rawString, activeGameName, color);
            drawer = new ChessboardDrawer(activeGame, pov);
            return formatted + drawer.drawBoard();
        }
        else {
            throw new ResponseException(407, "Expected: <id> [WHITE|BLACK]");
        }
    }

    public String observe(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length >= 1) {
            int displayedID = Integer.parseInt(params[0]);
            int trueID = displayedIDConverter.get(displayedID);
            ListResult listResult = server.list(authToken);
            for (GameData data : listResult.games()) {
                if (data.gameID() == trueID) {
                    activeGame = data.game();
                    activeGameName = data.gameName();
                    break;
                }
            }
            String observingMessage = String.format("Now observing game %s.%n", activeGameName);
            pov = ChessGame.TeamColor.WHITE;
            drawer = new ChessboardDrawer(activeGame, pov);
            return observingMessage + drawer.drawBoard();
        }
        else {
            throw new ResponseException(407, "Expected: <id>");
        }
    }

    public String statusDisplay() {
        if (state == State.LOGGEDOUT) {
            return "LOGGED_OUT";
        }
        if (state == State.LOGGEDIN) {
            return username;
        }
        if (state == State.INGAME) {
            return activeGameName;
        }
        return "";
    }

    private void assertLoggedIn() throws ResponseException {
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(409, "You must be logged in to use this command. Please use logIn or register first.");
        }
    }
}
