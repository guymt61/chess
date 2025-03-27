package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
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


    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command, session);
            case LEAVE -> leave(command);
            //case MAKE_MOVE -> ;
            //case RESIGN -> ;
        }
    }

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
            System.out.println(e.getMessage());
            return;
        }
        connections.remove(username);
        var message = String.format("%s left the game", username);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.setMessage(message);
        connections.broadcast(username, serverMessage);
    }

    private void makeMove(UserGameCommand command) throws IOException {}

    private String prettyMovePrinter(String username, ChessMove move, ChessGame game) {
        StringBuilder builder = new StringBuilder();
        builder.append(username);
        builder.append(" moved the ");
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = game.getBoard().getPiece(startPosition);
        builder.append(piece.getName());
        builder.append(" on ");
        builder.append(prettyPositionPrinter(startPosition));
        builder.append(" to ");
        builder.append(prettyPositionPrinter(endPosition));
        ChessPiece.PieceType promotion = move.getPromotionPiece();
        if (promotion != null) {
            builder.append(", promoting it to a ");
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

}