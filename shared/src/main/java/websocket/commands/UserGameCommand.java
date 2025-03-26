package websocket.commands;

import java.util.Objects;
import chess.ChessMove;

/**
 * Represents a command a user can send the server over a websocket
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;

    private final String authToken;

    private final Integer gameID;

    private ChessMove move;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public void setMove(ChessMove move) {
        if(commandType != CommandType.MAKE_MOVE) {
            String warningMessage = String.format("WARNING: Unexpected move added to %s UserGameCommand", commandType);
            System.out.println(warningMessage);
        }
        this.move = move;
    }

    public ChessMove getMove() {
        if(commandType != CommandType.MAKE_MOVE) {
            String warningMessage = String.format("WARNING: Unexpected move retrieved from %s UserGameCommand", commandType);
            System.out.println(warningMessage);
        }
        return move;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }
}
