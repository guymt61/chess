package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import requestsresults.JoinRequest;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

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

}