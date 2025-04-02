package client.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    String username;


    public WebSocketFacade(String url, NotificationHandler notificationHandler, String username) throws ResponseException {
        try {
            this.username = username;
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;


            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int id) throws ResponseException {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, id);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        }
        catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public void makeMove(String authToken, int id, ChessMove chessMove) throws ResponseException {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, id);
            String moveJson = new Gson().toJson(chessMove);
            command.setMove(moveJson);
            command.setUsername(username);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        }
        catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public void leave(String authToken, int id) throws ResponseException {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, id);
            command.setUsername(username);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            this.session.close();
        }
        catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public void resign(String authToken, int id) throws ResponseException {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, id);
            command.setUsername(username);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        }
        catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }
}
