package service;

import dataaccess.*;
import exception.ResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import model.*;
import requestsresults.*;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private GameService service;
    private final AuthData authedUser = new AuthData("token", "authedUser");
    private final GameData testGame1 = new GameData(1, null, null, "game1", null);
    private final GameData testGame2 = new GameData(2, null, null, "game2", null);

    @BeforeEach
    void setup() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        service = new GameService(gameDAO, authDAO);
        authDAO.createAuth(authedUser);
    }

    @Test
    @DisplayName("List Of No Games")
    void listEmpty() throws ResponseException{
        HashSet<GameData> emptySet = new HashSet<>();
        assertNotNull(service.list(new ListRequest("token")));
        HashSet<GameData> gamesList = service.list(new ListRequest("token")).games();
        assertEquals(emptySet, gamesList);
    }

    @Test
    @DisplayName("List Of Two Games")
    void listTwo() throws ResponseException {
        HashSet<GameData> twoGameSet = new HashSet<>();
        twoGameSet.add(testGame1);
        twoGameSet.add(testGame2);
        gameDAO.createGame(testGame1);
        gameDAO.createGame(testGame2);
        assertNotNull(service.list(new ListRequest("token")));
        HashSet<GameData> gamesList = service.list(new ListRequest("token")).games();
        assertEquals(twoGameSet, gamesList);
    }

    @Test
    @DisplayName("List With Invalid Auth")
    void listInvalid() {
        gameDAO.createGame(testGame1);
        try {
            service.list(new ListRequest("Fake Token"));
            fail("List should have thrown an error");
        } catch (ResponseException e) {
            assertEquals(401, e.StatusCode());
        }

    }

    @Test
    @DisplayName("Basic Create Game")
    void basicCreate() throws ResponseException{
        CreateResult result = service.create(new CreateRequest("Token", "game1"));
        assertNotNull(result);
        int createdID = result.gameID();
        GameData createdGame = gameDAO.getGame(createdID);
        assertNotNull(createdGame);
        assertEquals("game1", createdGame.gameName());
        assertNull(createdGame.whiteUsername());
        assertNull(createdGame.blackUsername());
        assertNotNull(createdGame.game());
    }

    @Test
    @DisplayName("Unauthorized Create")
    void unauthedCreate() {
        try {
            service.create(new CreateRequest("Fake Token", "game1"));
            fail("Create should have thrown an error");
        } catch (ResponseException e) {
            assertEquals(401, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Incomplete Create Request")
    void badCreate() {
        try {
            service.create(new CreateRequest("Token", null));
        } catch (ResponseException e) {
            assertEquals(400, e.StatusCode());
        }
    }

    @Test
    @DisplayName("Duplicated Name Create")
    void sameNameCreate() throws ResponseException{
        CreateResult result1 = service.create(new CreateRequest("Token", "TestGame"));
        CreateResult result2 = service.create(new CreateRequest("Token", "TestGame"));
        int ID1 = result1.gameID();
        GameData game1 = gameDAO.getGame(ID1);
        int ID2 = result2.gameID();
        GameData game2 = gameDAO.getGame(ID2);
        assertNotNull(game1);
        assertNotNull(game2);
        assertNotEquals(ID1, ID2, "IDs should not be the same");
        assertNotEquals(game1, game2, "Same game created twice");
    }

    @Test
    void join() {
    }
}