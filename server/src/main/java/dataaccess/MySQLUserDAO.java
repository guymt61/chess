package dataaccess;

import com.google.gson.Gson;
import exception.ResponseException;
import model.UserData;
import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

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
    public void createUser(UserData data) {
        var statement = "INSERT INTO pet (username, password, email) VALUES (?, ?, ?)";
        try {
            executeUpdate(statement, data.username(), data.password(), data.email());
        }
        catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }
    }


    private int executeUpdate(String statement, Object... params) throws ResponseException, DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
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

