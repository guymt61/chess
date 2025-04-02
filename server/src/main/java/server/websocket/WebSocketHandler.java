package server.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.AuthDAO;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.*;
import java.io.IOException;


@WebSocket
public class WebSocketHandler {

    private final GameService gameService;

    private final AuthDAO authDAO;

    private final ConnectionManager connections = new ConnectionManager();

    private boolean over = false;


    public WebSocketHandler(GameService gameService, AuthDAO authDAO) {
        this.gameService = gameService;
        this.authDAO = authDAO;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        try {
            command.setUsername(getUsername(command.getAuthToken()));
            command.setGameData(gameService.getGame(command.getGameID()));
            switch (command.getCommandType()) {
                case CONNECT -> connect(command, session);
                case LEAVE -> leave(command);
                case MAKE_MOVE -> makeMove(command);
                case RESIGN -> resign(command);
            }
        } catch (Exception e) {
            session.getRemote().sendString(e.getMessage());
        }
    }

    public void declareOver() {
        over = true;
    }

    private void connect(UserGameCommand command, Session session) throws IOException {
        GameData game = command.getGameData();
        String username = command.getUsername();
        String joinAs = "an observer";
        if (game.whiteUsername() != null) {
            if(game.whiteUsername().equals(username)) {
                joinAs = "white";
            }
        }
        if (game.blackUsername() != null) {
            if (game.blackUsername().equals(username)) {
                joinAs = "black";
            }
        }
        connections.add(username, session);
        var message = String.format("%s joined the game as %s.", username, joinAs);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.setMessage(message);
        connections.broadcast(username, serverMessage);
    }

    private void leave(UserGameCommand command) throws IOException {
        String username = command.getUsername();
        int id = command.getGameID();
        String authToken = command.getAuthToken();
        try {
            gameService.removePlayer(authToken, id);
        }
        catch (Exception e) {
            errorHandler(username, e);
            return;
        }
        connections.remove(username);
        var message = String.format("%s left the game", username);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.setMessage(message);
        connections.broadcast(username, serverMessage);
    }

    private void makeMove(UserGameCommand command) throws IOException {
        String username = command.getUsername();
        try {
            assertNotOver();
        } catch (Exception e) {
            errorHandler(username, e);
            return;
        }
        ChessMove move = new Gson().fromJson(command.getMove(), ChessMove.class);
        try {
            GameData afterMove = gameService.makeMove(command.getGameID(), move);
            var loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            var gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            loadMessage.setGame(gson.toJson(afterMove));
            connections.broadcast("", loadMessage);
            var notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notificationMessage.setMessage(prettyMovePrinter(username,move));
            connections.broadcast("", notificationMessage);
            checkGameState(afterMove);
        }
        catch (ResponseException e) {
            errorHandler(username, e);
        }

    }

    private String prettyMovePrinter(String username, ChessMove move) {
        StringBuilder builder = new StringBuilder();
        builder.append(username);
        builder.append(" moved from ");
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        builder.append(prettyPositionPrinter(startPosition));
        builder.append(" to ");
        builder.append(prettyPositionPrinter(endPosition));
        ChessPiece.PieceType promotion = move.getPromotionPiece();
        if (promotion != null) {
            builder.append(", promoting to a ");
            builder.append(new ChessPiece(ChessGame.TeamColor.WHITE, promotion).getName());
        }
        builder.append(".");
        return builder.toString();
    }

    private String prettyPositionPrinter(ChessPosition position) {
        String col = switch(position.getColumn()) {
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            case 6 -> "f";
            case 7 -> "g";
            case 8 -> "h";
            default -> "ERR";
        };
        int row = position.getRow();
        return col + row;
    }

    private void resign(UserGameCommand command) throws IOException {
        String username = command.getUsername();
        try {
            assertNotOver();
        }
        catch (Exception e) {
            errorHandler(username, e);
        }
        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.setMessage(String.format("%s has resigned", username));
        declareOver();
        connections.broadcast(username, serverMessage);
    }

    private void errorHandler(String username, Exception error) throws IOException {
        var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        serverErrorMessage.setErrorMessage(error.getMessage());
        connections.send(username, serverErrorMessage);
    }

    private void assertNotOver() throws Exception{
        if (over) {
            throw new Exception("This game is over");
        }
    }

    private void checkGameState(GameData gameData) throws IOException {
        ChessGame game = gameData.game();
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            ServerMessage whiteInCheckmate = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            whiteInCheckmate.setMessage(String.format("%s is in checkmate. %s wins!", white, black));
            connections.broadcast("", whiteInCheckmate);
            declareOver();
            return;
        }
        if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            ServerMessage blackInCheckmate = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            blackInCheckmate.setMessage(String.format("%s is in checkmate. %s wins!", black, white));
            connections.broadcast("", blackInCheckmate);
            declareOver();
            return;
        }
        if (game.isInStalemate(ChessGame.TeamColor.WHITE)) {
            ServerMessage whiteInStalemate = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            whiteInStalemate.setMessage(String.format("%s is in stalemate. Draw.", white));
            connections.broadcast("", whiteInStalemate);
            declareOver();
            return;
        }
        if (game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            ServerMessage blackInStalemate = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            blackInStalemate.setMessage(String.format("%s is in stalemate. Draw.", black));
            connections.broadcast("", blackInStalemate);
            declareOver();
            return;
        }
        if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            ServerMessage whiteInCheck = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            whiteInCheck.setMessage(String.format("%s is in check.", white));
        }
        if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            ServerMessage blackInCheck = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            blackInCheck.setMessage(String.format("%s is in check.", black));
        }
    }

    private String getUsername(String authToken) throws ResponseException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
        return authData.user();
    }

}