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

    private RegisterRequest regReqFromUserData(UserData data) {
        return new RegisterRequest(data.username(), data.password(), data.email());
    }

    private LoginRequest loginReqFromUserData(UserData data) {
        return new LoginRequest(data.username(), data.password());
    }

    private void checkForAuth(String authToken, String username){
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
        RegisterResult result = service.register(regReqFromUserData(testUser1));
        assertNotNull(userDAO.getUser("testUser1"));
        checkForAuth(result.authToken(), "testUser1");
    }

    @Test
    @DisplayName("Successful Registration - Occupied DAO")
    void successBusyRegister() throws ResponseException{
        userDAO.createUser(testUser2);
        userDAO.createUser(testUser3);
        userDAO.createUser(testUser4);
        userDAO.createUser(testUser5);
        RegisterResult result = service.register(regReqFromUserData(testUser1));
        assertNotNull(userDAO.getUser("testUser1"));
        checkForAuth(result.authToken(),"testUser1");
    }

    @Test
    @DisplayName("Triple Successful Registration")
    void tripleRegister() throws ResponseException {
        RegisterResult result1 = service.register(regReqFromUserData(testUser1));
        RegisterResult result2 = service.register(regReqFromUserData(testUser2));
        RegisterResult result3 = service.register(regReqFromUserData(testUser3));
        assertNotNull(userDAO.getUser("testUser1"));
        assertNotNull(userDAO.getUser("testUser2"));
        assertNotNull(userDAO.getUser("testUser3"));
        checkForAuth(result1.authToken(), "testUser1");
        checkForAuth(result2.authToken(), "testUser2");
        checkForAuth(result3.authToken(), "testUser3");
    }

    @Test
    @DisplayName("Register Username Already In Use")
    void registerTaken(){
        userDAO.createUser(testUser1);
        try {
            service.register(regReqFromUserData(testUser1));
            fail("Register did not throw an error");
        } catch (ResponseException e) {
            //ResponseException thrown as intended
            assertEquals(403, e.statusCode());
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
            assertEquals(400, e.statusCode());
        }
        //Missing password
        try {
            service.register(new RegisterRequest("user", null, "email"));
            fail("Register did not throw an error");
        } catch (ResponseException e) {
            //Exception thrown as intended
            assertEquals(400, e.statusCode());
        }
        //Missing email
        try {
            service.register(new RegisterRequest("user", "pass", null));
            fail("Register did not throw an error");
        } catch (ResponseException e) {
            //Exception thrown as intended
            assertEquals(400, e.statusCode());
        }

    }

    @Test
    @DisplayName("Successful Login")
    void loginSuccess() throws ResponseException{
        userDAO.createUser(testUser1);
        LoginResult result = service.login(loginReqFromUserData(testUser1));
        checkForAuth(result.authToken(), "testUser1");
    }

    @Test
    @DisplayName("Triple Login")
    void loginTriple() throws ResponseException{
        userDAO.createUser(testUser1);
        userDAO.createUser(testUser2);
        userDAO.createUser(testUser3);
        LoginResult result1 = service.login(loginReqFromUserData(testUser1));
        LoginResult result2 = service.login(loginReqFromUserData(testUser2));
        LoginResult result3 = service.login(loginReqFromUserData(testUser3));
        checkForAuth(result1.authToken(), "testUser1");
        checkForAuth(result2.authToken(), "testUser2");
        checkForAuth(result3.authToken(), "testUser3");
    }

    @Test
    @DisplayName("Incorrect Password Login")
    void incorrectPassword() {
        userDAO.createUser(testUser1);
        try {
            service.login(new LoginRequest("testUser1", "This isnt correct"));
            fail("Login was supposed to fail");
        } catch (ResponseException e) {
            assertEquals(401, e.statusCode());
        }
    }

    @Test
    @DisplayName("Nonexistent User Login")
    void nonexistentUser() {
        //Include some users to make the UserData non-trivial
        userDAO.createUser(testUser1);
        userDAO.createUser(testUser2);
        try {
            service.login(new LoginRequest("I don't exist", "superSecure"));
        } catch (ResponseException e) {
            assertEquals(401, e.statusCode());
        }
    }

    @Test
    @DisplayName("Same User Double Login")
    void sameLoginTwice() throws ResponseException{
        userDAO.createUser(testUser1);
        LoginResult result1 = service.login(loginReqFromUserData(testUser1));
        assertNotNull(authDAO.getAuth(result1.authToken()));
        LoginResult result2 = service.login(loginReqFromUserData(testUser1));
        assertNotEquals(result1.authToken(), result2.authToken(), "Both logins returned the same auth token");
        assertNotNull(authDAO.getAuth(result2.authToken()));
    }

    @Test
    @DisplayName("Logout Success")
    void logout() throws ResponseException{
        //Assumes that register functions properly
        RegisterResult registration = service.register(regReqFromUserData(testUser1));
        service.logout(new LogoutRequest(registration.authToken()));
        assertNull(authDAO.getAuth(registration.authToken()));
    }

    @Test
    @DisplayName("Logout with no AuthData stored")
    void emptyAuthLogout() {
        try {
            service.logout(new LogoutRequest("authToken"));
            fail("Logout should have thrown an error");
        } catch (ResponseException e) {
            assertEquals(401, e.statusCode());
            assertNull(authDAO.getAuth("authToken"));
        }
    }

    @Test
    @DisplayName("Logout with invalid token")
    void badAuthLogout() throws ResponseException{
        RegisterResult result1 = service.register(regReqFromUserData(testUser1));
        RegisterResult result2 = service.register(regReqFromUserData(testUser2));
        String auth1 = result1.authToken();
        String auth2 = result2.authToken();
        try {
            service.logout(new LogoutRequest("Fake Token"));
            fail("Logout should have thrown an error");
        } catch (ResponseException e) {
            assertEquals(401, e.statusCode());
            assertNotNull(authDAO.getAuth(auth1));
            assertNotNull(authDAO.getAuth(auth2));
            assertNull(authDAO.getAuth("Fake Token"));
        }
    }
}