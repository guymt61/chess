package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySQLGameDAOTest {

    private static GameDAO gameDAO;
    private final ChessGame rawGame1 = new ChessGame();
    //using a negative ID because no existing game will have it
    private final GameData gameData1 = new GameData(-1, "w", "b", "game1", rawGame1);

    @BeforeAll
    static void dupeTable() {
        try {
            gameDAO = new MySQLGameDAO();
        }
        catch (Exception e) {
            System.out.println(String.format("Test Setup Failed on account of %n", e));
        }
        var statement = "CREATE TABLE gamesCopy LIKE games";
        try (var conn = DatabaseManager.getConnection()) {
            var cloner = conn.prepareStatement(statement);
            cloner.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "INSERT INTO gamesCopy SELECT * FROM games";
        try (var conn = DatabaseManager.getConnection()) {
            var inserter = conn.prepareStatement(statement);
            inserter.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void replaceTable() {
        var statement = "DROP TABLE games";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "CREATE TABLE games LIKE gamesCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "INSERT INTO games SELECT * FROM gamesCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var inserter = conn.prepareStatement(statement);
            inserter.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statement = "DROP TABLE gamesCopy";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void clear() {
        gameDAO.clear();
        assertEquals(0, countRows());
    }

    @Test
    @DisplayName("Successful Create")
    void createGame() {
        int startingRowCount = countRows();
        gameDAO.createGame(gameData1);
        assertEquals(startingRowCount + 1, countRows());
    }

    @Test
    @DisplayName("Create Duplicate ID")
    void createDupeGame() {
        gameDAO.createGame(gameData1);
        int startingRowCount = countRows();
        gameDAO.createGame(gameData1);
        assertEquals(startingRowCount, countRows());
    }

    @Test
    @DisplayName("Get existing game")
    void getGame() {
        gameDAO.createGame(gameData1);
        assertNotNull(gameDAO.getGame(gameData1.gameID()));
        assertEquals(gameData1, gameDAO.getGame(gameData1.gameID()));
    }

    @Test
    void updateGame() {
    }

    @Test
    void listGames() {
    }

    private int countRows() {
        try (var conn = DatabaseManager.getConnection()) {
            var query = conn.prepareStatement("SELECT COUNT(*) AS rowCount FROM games");
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