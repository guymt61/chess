package dataaccess;
import model.GameData;

import java.util.HashMap;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {

    public MemoryGameDAO() {
        games = new HashMap<Integer, GameData>();
    }

    public void clear() {
        games.clear();
    }

    public void createGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public GameData getGame(int id) {
        return games.get(id);
    }

    public void updateGame(GameData game) throws DataAccessException{
        if (games.get(game.gameID()) == null) {
            throw new DataAccessException("Error: Cannot update a game that doesn't exist");
        }
        games.put(game.gameID(), game);
    }

    public HashSet<GameData> listGames() {
        return new HashSet<>(games.values());
    }

    private final HashMap<Integer, GameData> games;
}
