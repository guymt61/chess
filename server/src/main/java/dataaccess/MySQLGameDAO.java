package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;

import java.sql.*;
import java.util.HashSet;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLGameDAO implements GameDAO {

    public MySQLGameDAO() throws DataAccessException, ResponseException {
        configureDatabase();
    }

    //Clear all games
    public void clear() {
        var statement = "TRUNCATE games";
        try {
            executeUpdate(statement);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Add a new game
    public void createGame(GameData game) {
        var statement = "INSERT INTO games (id, whiteUsername, blackUsername, name, game) VALUES (?, ?, ?, ?, ?)";
        var json = new Gson().toJson(game.game());
        try {
            executeUpdate(statement, game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), json);
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
                ChessGame foundGame = new Gson().fromJson(rs.getString("game"), ChessGame.class);
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
            var json = new Gson().toJson(game.game());
            executeUpdate(statement, game.whiteUsername(), game.blackUsername(), json, game.gameID());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Produce a list of all GameData
    public HashSet<GameData> listGames() {
        HashSet<GameData> allGames = new HashSet<>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        GameData foundGame = getGame(rs.getInt("id"));
                        allGames.add(foundGame);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return allGames;
    }

    private final String[] createStatements = {
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

    private int executeUpdate(String statement, Object... params) throws ResponseException, DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof Integer integer) ps.setInt(i + 1, integer);
                    else if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

}
