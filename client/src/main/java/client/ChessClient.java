package client;
import exception.ResponseException;
import model.*;
import requestsresults.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ChessClient {

    private State state;
    private final ServerFacade server;
    private String authToken;
    private String username;
    private HashMap<Integer, Integer> displayedIDConverter;

    public ChessClient(String serverurl) {
        server = new ServerFacade(serverurl);
        state = State.SIGNEDOUT;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "signin" -> signIn(params);
                case "register" -> register(params);
                case "signOut" -> signOut();
                case "create" -> create(params);
                case "list" -> list();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    signIn <username> <password> - sign in to play as an existing user
                    register <username> <password> <email> - create a new user to play chess
                    help - list commands
                    quit - exit the chess client
                    """;
        }
        if (state == State.SIGNEDIN) {
            return """
                    signOut - sign out of this user
                    create <name> - create a new chess game
                    list - list all active chess games
                    join <id> [WHITE|BLACK] - join a chess game
                    observe <id> - watch a game without playing
                    help - list possible commands
                    """;
        }
        return null;
    }

    public String signIn(String... params) throws ResponseException {
        if (params.length >= 2) {
            LoginResult result = server.login(params[0], params[1]);
            if (result != null) {
                authToken = result.authToken();
                username = result.username();
                state = State.SIGNEDIN;
                return String.format("Signed in as %s", username);
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
                state = State.SIGNEDIN;
                return String.format("Registered and signed in as %s", username);
            }
            else {
                throw new ResponseException(407, "Register failed");
            }
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String signOut() throws ResponseException {
        assertSignedIn();
        server.logout(authToken);
        username = null;
        authToken = null;
        state = State.SIGNEDOUT;
        return "You have been signed out.";
    }

    public String create(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            String gameName = params[0];
            server.create(gameName, authToken);
            return String.format("New chess game created named '%s'", gameName);
        }
        throw new ResponseException(407, "Expected: <name>");
    }

    public String list() throws ResponseException {
        assertSignedIn();
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

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(409, "You must be signed in to use this command. Please use signIn or register first.");
        }
    }
}
