package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    private String message;
    private String errorMessage;
    private ChessGame game;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public void setMessage(String message) {
        if (serverMessageType != ServerMessageType.NOTIFICATION) {
            String warningMessage = String.format("WARNING: unexpected message added to a %s ServerMessage", serverMessageType);
            System.out.println();
        }
        this.message = message;
    }

    public String getMessage() {
        if (serverMessageType != ServerMessageType.NOTIFICATION) {
            String warningMessage = String.format("WARNING: unexpected message retrieved from a %s ServerMessage", serverMessageType);
            System.out.println(warningMessage);
        }
        return message;
    }

    public void setErrorMessage(String errorMessage) {
        if (serverMessageType != ServerMessageType.ERROR) {
            String warningMessage = String.format("WARNING: Unexpected error message added to %s ServerMessage", serverMessageType);
            System.out.println(warningMessage);
        }
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        if (serverMessageType != ServerMessageType.ERROR) {
            String warningMessage = String.format("WARNING: Unexpected error message retrieved from %s ServerMessage", serverMessageType);
            System.out.println(warningMessage);
        }
        return errorMessage;
    }

    public void setGame(ChessGame game) {
        if (serverMessageType != ServerMessageType.LOAD_GAME) {
            String warningMessage = String.format("WARNING: Unexpected game added to %s ServerMessage", serverMessageType);
            System.out.println(warningMessage);
        }
        this.game = game;
    }

    public ChessGame getGame() {
        if (serverMessageType != ServerMessageType.LOAD_GAME) {
            String warningMessage = String.format("WARNING: Unexpected game retrieved from %s ServerMessage", serverMessageType);
            System.out.println(warningMessage);
        }
        return game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
