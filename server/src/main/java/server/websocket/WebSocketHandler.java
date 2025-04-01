package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
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

    private final ConnectionManager connections = new ConnectionManager();

    private boolean over = false;


    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("WebSocketHandler received a message!");
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command, session);
            case LEAVE -> leave(command);
            case MAKE_MOVE -> makeMove(command);
            case RESIGN ->  resign(command);
        }
    }

    public void declareOver() {
        over = true;
    }

//    public void announceJoin(String username, String color) throws IOException {
//        String joinAs;
//        if (color == null) {
//            joinAs = "an observer.";
//        }
//        else {
//            String colorLowerCase = toString().toLowerCase();
//            if (colorLowerCase.equals("white") || colorLowerCase.equals("black")) {
//                joinAs = colorLowerCase+".";
//            }
//            else {
//                joinAs = "an observer.";
//            }
//        }
//        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
//        serverMessage.setMessage(String.format("%s joined the game as %s", username, joinAs));
//        connections.broadcast(username, serverMessage);
//    }

    private void connect(UserGameCommand command, Session session) throws IOException {
        String username = command.getUsername();
        String joinAs = switch (command.getConnectType()) {
            case OBSERVER -> "an observer";
            case BLACK -> "black";
            case WHITE -> "white";
        };
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
        }
        ChessMove move = new Gson().fromJson(command.getMove(), ChessMove.class);
        try {
            GameData afterMove = gameService.makeMove(command.getGameID(), move);
            var loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadMessage.setGame(new Gson().toJson(afterMove));
            connections.broadcast("", loadMessage);
            var notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notificationMessage.setMessage(prettyMovePrinter(username,move));
            connections.broadcast(username, notificationMessage);
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

}