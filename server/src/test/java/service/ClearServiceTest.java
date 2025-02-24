package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();
    private final UserData testUser1 = new UserData("testUser1", "superSecure", "test@test.test");
    private final AuthData testAuth1 = new AuthData("token1", "testUser1");
    private final GameData testGame1 = new GameData(1, null, null, "game1", null);
    private final UserData testUser2 = new UserData("testUser2", "superSecure", "test@test.test");
    private final AuthData testAuth2 = new AuthData("token2", "testUser2");
    private final GameData testGame2 = new GameData(2, null, null, "game2", null);
    private final UserData testUser3 = new UserData("testUser3", "superSecure", "test@test.test");
    private final AuthData testAuth3 = new AuthData("token3", "testUser3");
    private final GameData testGame3 = new GameData(3, null, null, "game3", null);

    @BeforeEach
    void fillInDAOs() {
        userDAO.createUser(testUser1);
        userDAO.createUser(testUser2);
        userDAO.createUser(testUser3);
        authDAO.createAuth(testAuth1);
        authDAO.createAuth(testAuth2);
        authDAO.createAuth(testAuth3);
        gameDAO.createGame(testGame1);
        gameDAO.createGame(testGame2);
        gameDAO.createGame(testGame3);
    }

    @Test
    @DisplayName("Clear All")
    void clearAll() {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
        assertNull(userDAO.getUser("testUser1"));
        assertNull(userDAO.getUser("testUser2"));
        assertNull(userDAO.getUser("testUser3"));
        assertNull(authDAO.getAuth("token1"));
        assertNull(authDAO.getAuth("token2"));
        assertNull(authDAO.getAuth("token3"));
        assertNull(gameDAO.getGame(1));
        assertNull(gameDAO.getGame(2));
        assertNull(gameDAO.getGame(3));
    }
}
