package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import model.GameData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class MySQLGameDAOTest {

    private static GameDAO gameDAO;
    private final ChessGame rawGame = new ChessGame();
    //using a negative ID because no existing game will have it
    private final GameData gameData1 = new GameData(-1, "w", "b", "game1", rawGame);
    private final GameData gameData2 = new GameData(-2, "w", "b", "game2", rawGame);
    private final GameData gameData3 = new GameData(-3, "w", "b", "game3", rawGame);

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
    @DisplayName("Get fake game")
    void getFakeGame() {
        gameDAO.createGame(gameData1); //Make database non-trivial
        assertNull(gameDAO.getGame(-20));
    }

    @Test
    @DisplayName("Update game only")
    void updateGame() {
        gameDAO.createGame(gameData2);
        ChessMove pawnMove = new ChessMove(new ChessPosition(2, 1), new ChessPosition(4, 1), null);
        try {
            ChessGame madeMoveGame = new ChessGame();
            madeMoveGame.makeMove(pawnMove);
            GameData updatedGame = new GameData(-2, "w", "b", "game1", madeMoveGame);
            gameDAO.updateGame(updatedGame);
            assertEquals(madeMoveGame, gameDAO.getGame(-2).game());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("Update usernames")
    void updateUsers() {
        gameDAO.createGame(gameData2);
        GameData newUsersGame = new GameData(-2, "changed", "changed","game2", rawGame);
        try {
            gameDAO.updateGame(newUsersGame);
        } catch (DataAccessException e) {
            fail(e.getMessage());
        }
        GameData updateResult = gameDAO.getGame(-2);
        assertEquals("changed", updateResult.whiteUsername());
        assertEquals("changed", updateResult.blackUsername());
    }

    @Test
    @DisplayName("Update fake game")
    void updateFakeGame() {
        gameDAO.createGame(gameData3);
        try {
            GameData fakeGame = new GameData(-4, "w", "b", "game1", rawGame);
            gameDAO.updateGame(fakeGame);
            fail("updateGame did not throw an error");
        } catch (DataAccessException e) {
            assertEquals(gameData3, gameDAO.getGame(-3));
        }
    }

    @Test
    @DisplayName("List with some games")
    void listGames() {
        gameDAO.clear(); //Testing order is being weird, so make sure no weird updated versions present
        gameDAO.createGame(gameData1);
        gameDAO.createGame(gameData2);
        gameDAO.createGame(gameData3);
        ArrayList<GameData> expectedGames = new ArrayList<>();
        expectedGames.add(gameData1);
        expectedGames.add(gameData2);
        expectedGames.add(gameData3);
        ArrayList<GameData> allGames = gameDAO.listGames();
        assertEquals(expectedGames, allGames);
    }

    @Test
    @DisplayName("List with no games")
    void emptyListGames() {
        gameDAO.clear();
        ArrayList<GameData> foundGames = gameDAO.listGames();
        assertTrue(foundGames.isEmpty());
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