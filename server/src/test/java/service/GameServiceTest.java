package service;

import dataaccess.*;
import exception.ResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import model.*;
import requestsresults.ListRequest;


import java.util.Collection;
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
    void create() {
    }

    @Test
    void join() {
    }
}