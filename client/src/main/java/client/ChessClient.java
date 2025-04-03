package client;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import model.*;
import requestsresults.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static ui.EscapeSequences.SET_TEXT_COLOR_RED;

public class ChessClient {

    private State state;
    private final String url;
    private final ServerFacade server;
    private String authToken;
    private String username;
    private HashMap<Integer, Integer> displayedIDConverter;
    private ChessGame activeGame;
    private String activeGameName;
    private int activeGameId;
    private ChessGame.TeamColor pov;
    private ChessboardDrawer drawer;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;

    public ChessClient(String serverurl, NotificationHandler handler) {
        server = new ServerFacade(serverurl);
        url = serverurl;
        state = State.LOGGEDOUT;
        notificationHandler = handler;
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
                case "quit", "leave" -> quit();
                case "redraw" -> redraw();
                case "move" -> makeMove(params);
                case "resign" -> resign(params);
                case "highlight" -> highlight(params);
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
                    help - list possible commands
                    redraw - redraw the chessboard (no highlights)
                    leave - exit the current game
                    move <start> <end> ?promotionPiece? - move the piece from start to end, if legal
                    resign - concede the game to your opponent
                    highlight <position> - redraw the chessboard, highlighting all moves that can be made from position
                    """;
        }
        if (state == State.OBSERVING) {
            return """
                    help - list possible commands
                    redraw - redraw the chessboard (no highlights)
                    leave - stop observing the current game
                    highlight <position> - redraw the chessboard, highlighting all moves that can be made from position
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
            throw new ResponseException(411, "Error: There are no active games to join");
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
                activateGame(trueID, listResult);
                String rawString = "Successfully joined game %s controlling %s.%n";
                String formatted = String.format(rawString, activeGameName, color);
                drawer = new ChessboardDrawer(activeGame, pov);
                return formatted;
            } catch (NumberFormatException e) {
                throw new ResponseException(415, "Error: id must be supplied as an integer");
            }
            catch (Exception e) {
                throw new ResponseException(500, e.getMessage());
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
            if (displayedIDConverter == null) {
                throw new ResponseException(411, "Error: Please call list to list the games available to observe first.");
            }
            if (displayedIDConverter.isEmpty()) {
                throw new ResponseException(411, "Error: There are no active games to observe");
            }
            try {
                int displayedID = Integer.parseInt(params[0]);
                if (displayedIDConverter.get(displayedID) == null) {
                    throw new ResponseException(410, "Error: Invalid id");
                }
                int trueID = displayedIDConverter.get(displayedID);
                ListResult listResult = server.list(authToken);
                activateGame(trueID, listResult);
            } catch (NumberFormatException e) {
                throw new ResponseException(415, "Error: id must be supplied as an integer");
            }
            catch (Exception e) {
                throw new ResponseException(500, e.getMessage());
            }
            String observingMessage = String.format("Now observing game %s.%n", activeGameName);
            pov = ChessGame.TeamColor.WHITE;
            drawer = new ChessboardDrawer(activeGame, pov);
            state = State.OBSERVING;
            return observingMessage;
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

    public String updateGame(GameData game) {
        activeGame = game.game();
        activeGameName = game.gameName();
        drawer = new ChessboardDrawer(activeGame, pov);
        return drawer.drawBoard();
    }

    public String quit() throws ResponseException {
        return switch (state) {
            case LOGGEDOUT -> "Thank you for using the chess client!";
            case LOGGEDIN -> logOut();
            case INGAME -> exitGame();
            case OBSERVING -> exitObservation();
        };
    }

    public String exitGame() throws ResponseException {
        ws.leave(authToken, activeGameId);
        String exitMessage = String.format("You have exited game %s. Thanks for playing!", activeGameName);
        ws = null;
        activeGame = null;
        activeGameName = null;
        activeGameId = -1;
        state = State.LOGGEDIN;
        pov = null;
        drawer = null;
        return exitMessage;
    }

    public String exitObservation() throws ResponseException  {
        ws.leave(authToken, activeGameId);
        String exitMessage = String.format("You are no longer observing game %s.", activeGameName);
        ws = null;
        activeGame = null;
        activeGameName = null;
        activeGameId = -1;
        state = State.LOGGEDIN;
        pov = null;
        drawer = null;
        return exitMessage;
    }

    public String redraw() throws ResponseException {
        if (state != State.INGAME && state != State.OBSERVING) {
            throw new ResponseException(423, "You must be playing or observing a game to use this command");
        }
        return drawer.drawBoard();
    }

    public String makeMove(String... params) throws ResponseException {
        if (state != State.INGAME) {
            throw new ResponseException(423, "You must be playing a game to use this command");
        }
        if (params.length == 2) {
            String startPosString = params[0];
            ChessPosition startPosition = positionFromString(startPosString);
            String endPosString = params[1];
            ChessPosition endPosition = positionFromString(endPosString);
            ChessMove move = new ChessMove(startPosition, endPosition, null);
            ws.makeMove(authToken, activeGameId, move);
            return "";
        }
        if (params.length == 3) {
            String startPosString = params[0];
            ChessPosition startPosition = positionFromString(startPosString);
            String endPosString = params[1];
            ChessPosition endPosition = positionFromString(endPosString);
            ChessPiece.PieceType promotion = pieceTypeFromString(params[2]);
            ChessMove move = new ChessMove(startPosition, endPosition, promotion);
            ws.makeMove(authToken, activeGameId, move);
            return "";
        }
        throw new ResponseException(407, "Expected: <start> <end> ?promotionPiece?");
    }

    public String resign(String... params) throws ResponseException{
        if (state != State.INGAME) {
            throw new ResponseException(423, "You must be playing a game to use this command");
        }
        if (params.length != 0) {
            throw new ResponseException(407, "Expected no parameters for resign");
        }
        ws.resign(authToken, activeGameId);
        return "";
    }

    public String highlight(String... params) throws ResponseException {
        if (state != State.INGAME && state != State.OBSERVING) {
            throw new ResponseException(423, "You must be playing or observing a game to use this command");
        }
        if (params.length == 1) {
            ChessPosition position = positionFromString(params[0]);
            return drawer.drawHighlighted(position);
        }
        throw new ResponseException(407, "Expected: <position>");
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

    private ChessPosition positionFromString(String positionString) throws ResponseException {
        if (positionString.length() != 2) {
            throw new ResponseException(421, String.format("%s is not a valid position", positionString));
        }
        char rowChar = positionString.charAt(1);
        String rowString = Character.toString(rowChar);
        int row;
        try {
            row = Integer.parseInt(rowString);
        } catch (NumberFormatException e) {
            throw new ResponseException(421, String.format("'%s' is not a valid row", rowString));
        }
        int col = switch (positionString.toLowerCase().charAt(0)) {
            case 'a' -> 1;
            case 'b' -> 2;
            case 'c' -> 3;
            case 'd' -> 4;
            case 'e' -> 5;
            case 'f' -> 6;
            case 'g' -> 7;
            case 'h' -> 8;
            default -> -1;
        };
        if (col == -1) {
            throw new ResponseException(421, String.format("'%s' is not a valid column", positionString.charAt(0)));
        }
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType pieceTypeFromString(String pieceString) throws ResponseException {
        return switch (pieceString.toLowerCase()) {
            case "king" -> ChessPiece.PieceType.KING;
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "pawn" -> ChessPiece.PieceType.PAWN;
            default -> throw new ResponseException(425, String.format("'%s' is not a valid piece type", pieceString));
        };
    }

    private void activateGame(int trueID, ListResult listResult) throws ResponseException {
        for (GameData data : listResult.games()) {
            if (data.gameID() == trueID) {
                activeGame = data.game();
                activeGameName = data.gameName();
                activeGameId = data.gameID();
                ws = new WebSocketFacade(url, notificationHandler, username);
                ws.connect(authToken, activeGameId);
                break;
            }
        }
    }
}
