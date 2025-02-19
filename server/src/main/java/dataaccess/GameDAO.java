package dataaccess;
import model.GameData;
import java.util.Collection;

public interface GameDAO {
    //Clear all games
    void clear();

    //Add a new game
    void createGame(GameData game);

    //Find a game based on ID
    GameData getGame(int ID);

    //Update the chess game at a specified ID
    void updateGame(GameData game);

    //Produce a list of all GameData
    Collection<GameData> listGames();

}
