package model;
import chess.*;

public class GameData {

    public GameData(int ID, String whiteUser, String blackUser, String name, ChessGame theGame) {
        gameID = ID;
        whiteUsername = whiteUser;
        blackUsername = blackUser;
        gameName = name;
        game = theGame;
    }

    public int getID() {
        return gameID;
    }

    public void setID(int newID) {
        gameID = newID;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public void setWhiteUsername(String newUsername) {
        whiteUsername = newUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public void setBlackUsername(String newUsername) {
        blackUsername = newUsername;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String newName) {
        gameName = newName;
    }

    public ChessGame getGame() {
        return game;
    }

    public void setGame(ChessGame newGame) {
        game = newGame;
    }



    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;
}
