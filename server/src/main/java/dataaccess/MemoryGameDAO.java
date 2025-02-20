package dataaccess;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

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

    public GameData getGame(int ID) {
        return games.get(ID);
    }

    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public Collection<GameData> listGames() {
        return games.values();
    }

    private final HashMap<Integer, GameData> games;
}
