package dataaccess;

import model.GameData;

import java.util.HashSet;

public class MySQLGameDAO implements GameDAO {
    //Clear all games
    public void clear() {}

    //Add a new game
    public void createGame(GameData game) {}

    //Find a game based on ID
    public GameData getGame(int id) {
        return null;
    }

    //Update the chess game at a specified ID
    public void updateGame(GameData game) throws DataAccessException {}

    //Produce a list of all GameData
    public HashSet<GameData> listGames() {
        return null;
    }

}
