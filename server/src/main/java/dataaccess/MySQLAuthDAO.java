package dataaccess;

import exception.ResponseException;
import model.AuthData;

public class MySQLAuthDAO implements AuthDAO {

    private final MySQLHelper helper = new MySQLHelper();

    public MySQLAuthDAO() throws DataAccessException, ResponseException {
        String[] createStatements = {
                """
CREATE TABLE IF NOT EXISTS `auths` (
`authToken` varchar(256) NOT NULL,
`username` varchar(256) NOT NULL,
PRIMARY KEY (`authToken`),
INDEX(username)
)
"""
        };
        helper.configureDatabase(createStatements);
    }

    //Clear all AuthData
    public void clear() {
        var statement = "TRUNCATE auths";
        try {
            helper.executeUpdate(statement);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Create a new authorization
    public void createAuth(AuthData auth) {
        var statement = "INSERT INTO auths (authToken, username) VALUES (?, ?)";
        try {
            helper.executeUpdate(statement, auth.authToken(), auth.user());
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
            helper.executeUpdate(statement, auth.authToken());
        }
        catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }
    }
}
