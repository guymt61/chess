package dataaccess;

import exception.ResponseException;
import model.UserData;
import java.sql.*;

public class MySQLUserDAO implements UserDAO {

    public MySQLUserDAO() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    public void clear() {}

    //Find userData based on username
    public UserData getUser(String username) {
        return null;
    }

    //Add a new user
    public void createUser(UserData data) {}

    private final String[] createStatements = {
            """
CREATE TABLE IF NOT EXISTS users (
'username' varchar(256) NOT NULL
'password' varchar(256) NOT NULL
'email' varchar(256) NOT NULL
PRIMARY KEY ('username')
INDEX(password)
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

