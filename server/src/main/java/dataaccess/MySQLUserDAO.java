package dataaccess;

import exception.ResponseException;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;


public class MySQLUserDAO implements UserDAO {

    private final MySQLHelper helper = new MySQLHelper();

    public MySQLUserDAO() throws ResponseException, DataAccessException {
        String[] createStatements = {
                """
CREATE TABLE IF NOT EXISTS `users` (
  `username` VARCHAR(100) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`username`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE);
"""
        };
        helper.configureDatabase(createStatements);
    }

    public void clear() {
        var statement = "TRUNCATE users";
        try {
            helper.executeUpdate(statement);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Find userData based on username
    //Password will be hashed
    public UserData getUser(String username) {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users WHERE username=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                    }
                }
            }
        }
        catch (Exception e) {
            return null;
        }
        return null;
    }

    //Add a new user
    public void createUser(UserData data) {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try {
            String hashedPass = BCrypt.hashpw(data.password(), BCrypt.gensalt());
            helper.executeUpdate(statement, data.username(), hashedPass, data.email());
        }
        catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }
    }
}

