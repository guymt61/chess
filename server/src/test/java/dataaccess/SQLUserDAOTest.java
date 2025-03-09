package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {

    private UserDAO userDAO;
    private final UserData newUser1 = new UserData("testUser1", "superSecure", "test@test.test");
    private final UserData newUser2 = new UserData("testUser2", "superSecure", "test@test.test");
    private final UserData newUser3 = new UserData("testUser3", "superSecure", "test@test.test");

    @Test
    @Order(1)
    @DisplayName("Successful Create")
    void createUser() {
        startTransaction();
        assertDoesNotThrow(() -> userDAO.createUser(newUser1));
        rollback();
    }

    @Test
    @Order(2)
    @DisplayName("Username in Use")
    void createDupeUser() {
        startTransaction();
        userDAO.createUser(newUser1);
        userDAO.createUser(newUser1);
        rollback();

    }

    @Test
    @Order(3)
    void getUser() {

    }

    @Test
    @Order(5)
    void clear() {
        startTransaction();
        userDAO.clear();
        rollback();
    }

    void startTransaction() {
        var statement = "START TRANSACTION";
        try (var conn = DatabaseManager.getConnection()) {
            userDAO = new MySQLUserDAO();
            var startStatement = conn.prepareStatement(statement);
            startStatement.execute();
        }
        catch (Exception e) {
            System.out.println("Error when starting transaction");
        }
    }
    void rollback() {
        var statement = "ROLLBACK";
        try (var conn = DatabaseManager.getConnection()) {
            var startStatement = conn.prepareStatement(statement);
            startStatement.execute();
        }
        catch (Exception e) {
            System.out.println("Rollback check database integrity");
        }
    }

}