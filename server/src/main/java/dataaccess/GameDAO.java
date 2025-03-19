package dataaccess;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    //Clear all games
    void clear();

    //Add a new game
    void createGame(GameData game);

    //Find a game based on ID
    GameData getGame(int id);

    //Update the chess game at a specified ID
    void updateGame(GameData game) throws DataAccessException;

    //Produce a list of all GameData
    ArrayList<GameData> listGames();

}
