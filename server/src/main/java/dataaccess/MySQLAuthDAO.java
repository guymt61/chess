package dataaccess;

import exception.ResponseException;
import model.AuthData;
import java.sql.*;

public class MySQLAuthDAO implements AuthDAO {

    public MySQLAuthDAO() throws DataAccessException, ResponseException {
        configureDatabase();
    }

    //Clear all AuthData
    public void clear() {}

    //Create a new authorization
    public void createAuth(AuthData auth) {}

    //Retrieve an authorization by its authToken
    public AuthData getAuth(String authToken) {
        return null;
    }

    //Delete an authorization, invalidating it
    public void deleteAuth(AuthData auth) {}

    private final String[] createStatements = {
            """
CREATE TABLE IF NOT EXISTS auths (
'authToken' varchar(256) NOT NULL
'username' varchar(256) NOT NULL
PRIMARY KEY ('authToken')
INDEX(username)
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
