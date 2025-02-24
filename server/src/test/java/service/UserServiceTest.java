package service;

import dataaccess.*;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import requestsresults.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService service;
    private final UserData testUser1 = new UserData("testUser1", "superSecure", "test@test.test");
    private final UserData testUser2 = new UserData("testUser2", "superSecure", "test@test.test");
    private final UserData testUser3 = new UserData("testUser3", "superSecure", "test@test.test");
    private final UserData testUser4 = new UserData("testUser4", "superSecure", "test@test.test");
    private final UserData testUser5 = new UserData("testUser5", "superSecure", "test@test.test");

    private RegisterRequest RegReqFromUserData(UserData data) {
        return new RegisterRequest(data.username(), data.password(), data.email());
    }

    private void CheckForAuth(String authToken, String username){
        //Asserts that the authDAO has an authorization tied to authToken and that it is associated with username
        AuthData authData = authDAO.getAuth(authToken);
        assertNotNull(authData);
        assertEquals(username, authData.user());
    }

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        service = new UserService(userDAO, authDAO);
    }

    @Test
    @DisplayName("Successful Registration - Empty DAO")
    void successRegister() throws ResponseException {
        RegisterResult result = service.register(RegReqFromUserData(testUser1));
        assertNotNull(userDAO.getUser("testUser1"));
        CheckForAuth(result.authToken(), "testUser1");
    }

    @Test
    @DisplayName("Successful Registration - Occupied DAO")
    void successBusyRegister() throws ResponseException{
        userDAO.createUser(testUser2);
        userDAO.createUser(testUser3);
        userDAO.createUser(testUser4);
        userDAO.createUser(testUser5);
        RegisterResult result = service.register(RegReqFromUserData(testUser1));
        assertNotNull(userDAO.getUser("testUser1"));
        CheckForAuth(result.authToken(),"testUser1");
    }

    @Test
    @DisplayName("Triple Successful Registration")
    void tripleRegister() throws ResponseException {
        RegisterResult result1 = service.register(RegReqFromUserData(testUser1));
        RegisterResult result2 = service.register(RegReqFromUserData(testUser2));
        RegisterResult result3 = service.register(RegReqFromUserData(testUser3));
        assertNotNull(userDAO.getUser("testUser1"));
        assertNotNull(userDAO.getUser("testUser2"));
        assertNotNull(userDAO.getUser("testUser3"));
        CheckForAuth(result1.authToken(), "testUser1");
        CheckForAuth(result2.authToken(), "testUser2");
        CheckForAuth(result3.authToken(), "testUser3");
    }

    @Test
    @DisplayName("Register Username Already In Use")
    void registerTaken(){
        userDAO.createUser(testUser1);
        try {
            service.register(RegReqFromUserData(testUser1));
            fail("Register did not throw an error");
        } catch (ResponseException e) {
            //ResponseException thrown as intended
            assertEquals(403, e.StatusCode());
        }

    }

    @Test
    @DisplayName("Incomplete Registration")
    void registerIncomplete() {
        //Missing username
        try {
            service.register(new RegisterRequest(null, "pass", "email"));
            fail("Register did not throw an error");
        } catch (ResponseException e) {
            //Exception thrown as intended
            assertEquals(400, e.StatusCode());
        }
        //Missing password
        try {
            service.register(new RegisterRequest("user", null, "email"));
            fail("Register did not throw an error");
        } catch (ResponseException e) {
            //Exception thrown as intended
            assertEquals(400, e.StatusCode());
        }
        //Missing email
        try {
            service.register(new RegisterRequest("user", "pass", null));
            fail("Register did not throw an error");
        } catch (ResponseException e) {
            //Exception thrown as intended
            assertEquals(400, e.StatusCode());
        }

    }

    @Test
    void login() {
    }

    @Test
    void logout() {
    }
}