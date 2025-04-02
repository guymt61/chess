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
import java.util.HashMap;


@WebSocket
public class WebSocketHandler {

    private final GameService gameService;

    private final AuthDAO authDAO;

    private final ConnectionManager connections = new ConnectionManager();

    private final HashMap<Integer, Boolean> activeGames = new HashMap<>();


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
                case MAKE_MOVE -> makeMove(command, session);
                case RESIGN -> resign(command, session);
            }
        } catch (Exception e) {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage(e.getMessage());
            session.getRemote().sendString(new Gson().toJson(errorMessage));
        }
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
        connections.add(game.gameID(), username, session);
        var message = String.format("%s joined the game as %s.", username, joinAs);
        activeGames.putIfAbsent(game.gameID(), true);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.setMessage(message);
        connections.broadcast(game.gameID(), username, serverMessage);
        ServerMessage loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        var gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        loadMessage.setGame(gson.toJson(game));
        session.getRemote().sendString(new Gson().toJson(loadMessage));
    }

    private void leave(UserGameCommand command) throws IOException {
        String username = command.getUsername();
        int id = command.getGameID();
        String authToken = command.getAuthToken();
        try {
            gameService.removePlayer(authToken, id);
        }
        catch (Exception e) {
            errorHandler(id, username, e);
            return;
        }
        connections.remove(command.getGameID(), username);
        var message = String.format("%s left the game", username);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.setMessage(message);
        connections.broadcast(id, username, serverMessage);
    }

    private void makeMove(UserGameCommand command, Session session) throws IOException {
        String username = command.getUsername();
        GameData gameData = command.getGameData();
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();
        int gameId = command.getGameID();
        ChessGame chessGame = gameData.game();
        if (!username.equals(white) && !username.equals(black)) {
            ServerMessage observerError = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            observerError.setErrorMessage("You are not a player in this game");
            session.getRemote().sendString(new Gson().toJson(observerError));
            return;
        }
        String whoseTurn = switch (chessGame.getTeamTurn()) {
            case WHITE -> "WHITE";
            case BLACK -> "BLACK";
        };
        if ((username.equals(white) && whoseTurn.equals("BLACK")) || (username.equals(black) && whoseTurn.equals("WHITE"))) {
            ServerMessage wrongTurnError = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            wrongTurnError.setErrorMessage("It is not your turn");
            session.getRemote().sendString(new Gson().toJson(wrongTurnError));
            return;
        }
        if (!activeGames.get(gameId)) {
            ServerMessage gameOverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            gameOverError.setErrorMessage("This game is over.");
            session.getRemote().sendString(new Gson().toJson(gameOverError));
            return;
        }
        ChessMove move = command.getMove();
        try {
            GameData afterMove = gameService.makeMove(command.getGameID(), move);
            var loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            var gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            loadMessage.setGame(gson.toJson(afterMove));
            connections.broadcast(gameId,"", loadMessage);
            var notificationMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notificationMessage.setMessage(prettyMovePrinter(username,move));
            connections.broadcast(gameId, username, notificationMessage);
            checkGameState(afterMove);
        }
        catch (ResponseException e) {
            errorHandler(gameId, username, e);
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

    private void resign(UserGameCommand command, Session session) throws IOException {
        String username = command.getUsername();
        GameData game = command.getGameData();
        int gameId = command.getGameID();
        if (!activeGames.get(gameId)) {
            ServerMessage gameOverError = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            gameOverError.setErrorMessage("This game is over.");
            session.getRemote().sendString(new Gson().toJson(gameOverError));
            return;
        }
        if (username.equals(game.whiteUsername()) || username.equals(game.blackUsername())) {
            ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            serverMessage.setMessage(String.format("%s has resigned", username));
            activeGames.replace(gameId, false);
            connections.broadcast(gameId, "", serverMessage);
        }
        else {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("You cannot resign as an observer");
            session.getRemote().sendString(new Gson().toJson(errorMessage));
        }
    }

    private void errorHandler(int gameId, String username, Exception error) throws IOException {
        var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        serverErrorMessage.setErrorMessage(error.getMessage());
        connections.send(gameId, username, serverErrorMessage);
    }

    private void checkGameState(GameData gameData) throws IOException {
        ChessGame game = gameData.game();
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            ServerMessage whiteInCheckmate = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            whiteInCheckmate.setMessage(String.format("%s is in checkmate. %s wins!", white, black));
            connections.broadcast(gameData.gameID(), "", whiteInCheckmate);
            activeGames.replace(gameData.gameID(), false);
            return;
        }
        if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            ServerMessage blackInCheckmate = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            blackInCheckmate.setMessage(String.format("%s is in checkmate. %s wins!", black, white));
            connections.broadcast(gameData.gameID(),"", blackInCheckmate);
            activeGames.replace(gameData.gameID(), false);
            return;
        }
        if (game.isInStalemate(ChessGame.TeamColor.WHITE)) {
            ServerMessage whiteInStalemate = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            whiteInStalemate.setMessage(String.format("%s is in stalemate. Draw.", white));
            connections.broadcast(gameData.gameID(),"", whiteInStalemate);
            activeGames.replace(gameData.gameID(), false);
            return;
        }
        if (game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            ServerMessage blackInStalemate = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            blackInStalemate.setMessage(String.format("%s is in stalemate. Draw.", black));
            connections.broadcast(gameData.gameID(),"", blackInStalemate);
            activeGames.replace(gameData.gameID(), false);
            return;
        }
        if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            ServerMessage whiteInCheck = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            whiteInCheck.setMessage(String.format("%s is in check.", white));
            connections.broadcast(gameData.gameID(), "", whiteInCheck);
        }
        if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            ServerMessage blackInCheck = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            blackInCheck.setMessage(String.format("%s is in check.", black));
            connections.broadcast(gameData.gameID(), "", blackInCheck);
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