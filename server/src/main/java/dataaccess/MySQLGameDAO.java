package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exception.ResponseException;
import model.GameData;

import java.util.ArrayList;
import java.util.HashSet;


public class MySQLGameDAO implements GameDAO {

    private final MySQLHelper helper = new MySQLHelper();

    public MySQLGameDAO() throws DataAccessException, ResponseException {
        String[] createStatements = {
                """
CREATE TABLE IF NOT EXISTS games (
`id` int NOT NULL,
`whiteUsername` varchar(256),
`blackUsername` varchar(256),
`name` varchar(256) NOT NULL,
`game` text NOT NULL,
PRIMARY KEY (`id`),
INDEX(id)
)
"""
        };
        helper.configureDatabase(createStatements);
    }

    //Clear all games
    public void clear() {
        var statement = "TRUNCATE games";
        try {
            helper.executeUpdate(statement);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Add a new game
    public void createGame(GameData game) {
        var statement = "INSERT INTO games (id, whiteUsername, blackUsername, name, game) VALUES (?, ?, ?, ?, ?)";
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        var json = gson.toJson(game.game());
        try {
            helper.executeUpdate(statement, game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), json);
        }
        catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }
    }

    //Find a game based on ID
    public GameData getGame(int id) {
        var statement = "SELECT * FROM games WHERE id=?";
        try (var conn = DatabaseManager.getConnection()) {
            var ps = conn.prepareStatement(statement);
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            if (rs.next()) {
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");
                String name = rs.getString("name");
                var json = rs.getString("game");
                ChessGame foundGame = new Gson().fromJson(json, ChessGame.class);
                return new GameData(id, white, black, name, foundGame);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    //Update the chess game at a specified ID
    public void updateGame(GameData game) throws DataAccessException {
        if (getGame(game.gameID()) == null) {
            throw new DataAccessException("Cannot update game that doesn't exist");
        }
        var statement = "UPDATE games SET whiteUsername=?, blackUsername=?, game=? WHERE id=?";
        try {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            var json = gson.toJson(game.game());
            helper.executeUpdate(statement, game.whiteUsername(), game.blackUsername(), json, game.gameID());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Produce a list of all GameData
    public ArrayList<GameData> listGames() {
        ArrayList<GameData> allGames = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String white = rs.getString("whiteUsername");
                        String black = rs.getString("blackUsername");
                        String name = rs.getString("name");
                        var json = rs.getString("game");
                        ChessGame foundGame = new Gson().fromJson(json, ChessGame.class);
                        allGames.add(new GameData(id, white, black, name, foundGame));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return allGames;
    }

}
