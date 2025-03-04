package dataaccess;

import exception.ResponseException;
import model.GameData;

import java.sql.*;
import java.util.HashSet;

public class MySQLGameDAO implements GameDAO {

    public MySQLGameDAO() throws DataAccessException, ResponseException {
        configureDatabase();
    }

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

    private final String[] createStatements = {
            """
CREATE TABLE IF NOT EXISTS games (
'id' int NOT NULL
'whiteUsername' varchar(256)
'blackUsername' varchar(256)
'name' varchar(256) NOT NULL
'game' text NOT NULL
PRIMARY KEY ('id')
)
"""
    };

    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

}
