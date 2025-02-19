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
        games.put(game.getID(), game);
    }

    public GameData getGame(int ID) {
        return games.get(ID);
    }

    public void updateGame(int ID, GameData game) {
        games.put(ID, game);
    }

    public Collection<GameData> listGames() {
        return games.values();
    }

    private HashMap<Integer, GameData> games;
}
