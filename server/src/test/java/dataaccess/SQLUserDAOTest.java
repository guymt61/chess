package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {

    private static UserDAO userDAO;
    private final UserData newUser1 = new UserData("testUser1", "superSecure", "test@test.test");
    private final UserData newUser2 = new UserData("testUser2", "superSecure", "test@test.test");

    @BeforeAll
    static void dupeTable() {
        try {
            userDAO = new MySQLUserDAO();
        }
        catch (Exception e) {
            System.out.println(String.format("Test Setup Failed on account of %n", e));
        }
        var statement = "CREATE TABLE usersCopy LIKE users";
        try (var conn = DatabaseManager.getConnection()) {
            var cloner = conn.prepareStatement(statement);
            cloner.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "INSERT INTO usersCopy SELECT * FROM users";
        try (var conn = DatabaseManager.getConnection()) {
            var inserter = conn.prepareStatement(statement);
            inserter.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void replaceTable() {
        var statement = "DROP TABLE users";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "CREATE TABLE users LIKE usersCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "INSERT INTO users SELECT * FROM usersCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var inserter = conn.prepareStatement(statement);
            inserter.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "DROP TABLE usersCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Successful Create")
    void createUser() {
        assertDoesNotThrow(() -> userDAO.createUser(newUser1));
    }

    @Test
    @Order(2)
    @DisplayName("Username in Use")
    void createDupeUser() {
        userDAO.createUser(newUser2);
        int startingRowsCount = countRows();
        userDAO.createUser(newUser2);
        int currentRowsCount = countRows();
        assertEquals(startingRowsCount,currentRowsCount, "Number of rows changed after adding existing user");

    }

    @Test
    @Order(3)
    @DisplayName("Get User That Exists")
    void getUser() {
        System.out.println(countRows());
        assertNotNull(userDAO.getUser(newUser2.username()));
    }

    @Test
    @Order(4)
    @DisplayName("Get User That Doesn't Exist")
    void getFakeUser() {
        assertNull(userDAO.getUser("fakeyMcFakePants"));
    }

    @Test
    @Order(5)
    void clear() {
        userDAO.clear();
        assertEquals(0, countRows());
    }

    private int countRows() {
        try (var conn = DatabaseManager.getConnection()) {
            var query = conn.prepareStatement("SELECT COUNT(*) AS rowCount FROM users");
            var response = query.executeQuery();
            if (response.next()) {
                return response.getInt(1);
            }
            else {
                return 0;
            }
        } catch (Exception e) {
            fail(e.getMessage());
            return -1;
        }
    }

}