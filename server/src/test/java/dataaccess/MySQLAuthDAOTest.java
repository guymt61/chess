package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySQLAuthDAOTest {

    private static AuthDAO authDAO;
    private final AuthData newAuth1 = new AuthData("token1", "testUser1");
    private final AuthData newAuth2 = new AuthData("token2", "testUser2");

    @BeforeAll
    static void dupeTable() {
        try {
            authDAO = new MySQLAuthDAO();
        }
        catch (Exception e) {
            System.out.println(String.format("Test Setup Failed on account of %n", e));
        }
        var statement = "CREATE TABLE authsCopy LIKE auths";
        try (var conn = DatabaseManager.getConnection()) {
            var cloner = conn.prepareStatement(statement);
            cloner.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "INSERT INTO authsCopy SELECT * FROM auths";
        try (var conn = DatabaseManager.getConnection()) {
            var inserter = conn.prepareStatement(statement);
            inserter.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void replaceTable() {
        var statement = "DROP TABLE auths";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "CREATE TABLE auths LIKE authsCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "INSERT INTO auths SELECT * FROM authsCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var inserter = conn.prepareStatement(statement);
            inserter.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "DROP TABLE authsCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void clear() {
        authDAO.clear();
        assertEquals(0, countRows());
    }

    @Test
    @Order(2)
    @DisplayName("Successful Create")
    void createAuth() {
        int startingRowCount = countRows();
        authDAO.createAuth(newAuth1);
        assertEquals(startingRowCount + 1, countRows());
    }

    @Test
    @Order(3)
    @DisplayName("Token in use")
    void badCreateAuth() {
        authDAO.createAuth(newAuth2);
        int startingRowCount = countRows();
        authDAO.createAuth(newAuth2);
        assertEquals(startingRowCount, countRows());
    }

    @Test
    @Order(4)
    @DisplayName("Get Auth that exists")
    void getAuth() {
        authDAO.createAuth(newAuth1);
        assertNotNull(authDAO.getAuth("token1"));
        assertEquals(newAuth1, authDAO.getAuth("token1"));
    }

    @Test
    @Order(5)
    @DisplayName("Get Auth that doesn't exist")
    void getFakeAuth() {
        authDAO.createAuth(newAuth1); //Put something in the database so returning null is non-trivial
        assertNull(authDAO.getAuth("FakeSoFake"));
    }

    @Test
    @Order(6)
    @DisplayName("Delete existing auth")
    void deleteAuth() {
        authDAO.createAuth(newAuth1);
        int startingRowCount = countRows();
        authDAO.deleteAuth(newAuth1);
        assertEquals(startingRowCount - 1, countRows());
    }

    @Test
    @Order(7)
    @DisplayName("Delete nonexistent auth")
    void deleteFakeAuth() {
        authDAO.createAuth(newAuth2); //Make db non-trivial
        int startingRowCount = countRows();
        authDAO.deleteAuth(new AuthData("I don't", "exist"));
        assertEquals(startingRowCount, countRows());
    }

    private int countRows() {
        try (var conn = DatabaseManager.getConnection()) {
            var query = conn.prepareStatement("SELECT COUNT(*) AS rowCount FROM auths");
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