package dataaccess;

import exception.ResponseException;
import model.AuthData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySQLAuthDAO implements AuthDAO {

    public MySQLAuthDAO() throws DataAccessException, ResponseException {
        configureDatabase();
    }

    //Clear all AuthData
    public void clear() {
        var statement = "TRUNCATE auths";
        try {
            executeUpdate(statement);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Create a new authorization
    public void createAuth(AuthData auth) {
        var statement = "INSERT INTO auths (authToken, username) VALUES (?, ?)";
        try {
            executeUpdate(statement, auth.authToken(), auth.user());
        }
        catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }
    }

    //Retrieve an authorization by its authToken
    public AuthData getAuth(String authToken) {
        var statement = "SELECT * FROM auths WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection()){
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    return new AuthData(rs.getString("authToken"), rs.getString("username"));
                }
            }
        }
        catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    //Delete an authorization, invalidating it
    public void deleteAuth(AuthData auth) {
        var statement = "DELETE FROM auths WHERE authToken=?";
        try {
            executeUpdate(statement, auth.authToken());
        }
        catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }
    }

    private final String[] createStatements = {
            """
CREATE TABLE IF NOT EXISTS `auths` (
`authToken` varchar(256) NOT NULL,
`username` varchar(256) NOT NULL,
PRIMARY KEY (`authToken`),
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

    private int executeUpdate(String statement, Object... params) throws ResponseException, DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    }
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
