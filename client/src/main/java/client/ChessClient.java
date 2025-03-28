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
                case "logout" -> logOut(params);
                case "create" -> create(params);
                case "list" -> list(params);
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "quit" -> quit();
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
        if (state == State.INGAME) {
            return """
                    quit - exit the current game
                    help - list possible commands
                    """;
        }
        return null;
    }

    public String logIn(String... params) throws ResponseException {
        assertNotInGame();
        if (params.length == 2) {
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
        assertNotInGame();
        if (params.length == 3) {
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

    public String logOut(String... params) throws ResponseException {
        assertLoggedIn();
        assertNotInGame();
        if (params.length != 0) {
            throw new ResponseException(407, "Expected no parameters for logOut");
        }
        server.logout(authToken);
        username = null;
        authToken = null;
        state = State.LOGGEDOUT;
        return "You have been logged out.";
    }

    public String create(String... params) throws ResponseException {
        assertLoggedIn();
        assertNotInGame();
        if (params.length == 1) {
            String gameName = params[0];
            server.create(gameName, authToken);
            return String.format("New chess game created named '%s'", gameName);
        }
        throw new ResponseException(407, "Expected: <name>");
    }

    public String list(String... params) throws ResponseException {
        assertLoggedIn();
        assertNotInGame();
        if (params.length != 0) {
            throw new ResponseException(407, "Expected no parameters for list");
        }
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
        assertNotInGame();
        if (displayedIDConverter == null) {
            throw new ResponseException(411, "Error: Please call list to list the games available to join first.");
        }
        if (displayedIDConverter.isEmpty()) {
            throw new ResponseException(411, "Error: There are active games to join");
        }
        if (params.length == 2) {
            try {
                int displayedID = Integer.parseInt(params[0]);
                if (displayedIDConverter.get(displayedID) == null) {
                    throw new ResponseException(410, "Error: Invalid id");
                }
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
            } catch (NumberFormatException e) {
                throw new ResponseException(415, "Error: id must be supplied as an integer");
            }
        }
        else {
            throw new ResponseException(407, "Expected: <id> [WHITE|BLACK]");
        }
    }

    public String observe(String... params) throws ResponseException {
        assertLoggedIn();
        assertNotInGame();
        if (params.length == 1) {
            try {
                int displayedID = Integer.parseInt(params[0]);
                if (displayedIDConverter.get(displayedID) == null) {
                    throw new ResponseException(410, "Error: Invalid id");
                }
                int trueID = displayedIDConverter.get(displayedID);
                ListResult listResult = server.list(authToken);
                for (GameData data : listResult.games()) {
                    if (data.gameID() == trueID) {
                        activeGame = data.game();
                        activeGameName = data.gameName();
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                throw new ResponseException(415, "Error: id must be supplied as an integer");
            }
            String observingMessage = String.format("Now observing game %s.%n", activeGameName);
            pov = ChessGame.TeamColor.WHITE;
            drawer = new ChessboardDrawer(activeGame, pov);
            state = State.OBSERVING;
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
        if (state == State.OBSERVING) {
            return activeGameName;
        }
        return "";
    }

    public String quit() throws ResponseException {
        return switch (state) {
            case LOGGEDOUT -> "Thank you for using the chess client!";
            case LOGGEDIN -> logOut();
            case INGAME -> exitGame();
            case OBSERVING -> exitObservation();
        };
    }

    public String exitGame() {
        String exitMessage = String.format("You have exited game %s. Thanks for playing!", activeGameName);
        //Add code to remove player from the game
        activeGame = null;
        activeGameName = null;
        state = State.LOGGEDIN;
        pov = null;
        drawer = null;
        return exitMessage;
    }

    public String exitObservation() {
        String exitMessage = String.format("You are no longer observing game %s.", activeGameName);
        activeGame = null;
        activeGameName = null;
        state = State.LOGGEDIN;
        pov = null;
        drawer = null;
        return exitMessage;
    }

    private void assertLoggedIn() throws ResponseException {
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(409, "You must be logged in to use this command. Please use logIn or register first.");
        }
    }

    private void assertNotInGame() throws ResponseException {
        if (state == State.INGAME || state == State.OBSERVING) {
            String messageLine1 = "You cannot use this command while playing or observing a game.\n";
            String messageLine2 = "Use help to list possible commands or quit to exit the game.";
            throw new ResponseException(413, messageLine1 + messageLine2);
        }
    }
}
