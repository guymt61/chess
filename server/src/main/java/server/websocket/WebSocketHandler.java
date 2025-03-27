package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command, session);
            case LEAVE -> leave(command.getUsername());
            case MAKE_MOVE -> ;
            case RESIGN -> ;
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

    private void leave(String visitorName) throws IOException {
        connections.remove(visitorName);
        var message = String.format("%s left the shop", visitorName);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.setMessage(message);
        connections.broadcast(visitorName, serverMessage);
    }

    public void makeNoise(String petName, String sound) throws ResponseException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new Notification(Notification.Type.NOISE, message);
            connections.broadcast("", notification);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}