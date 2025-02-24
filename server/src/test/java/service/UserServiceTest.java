package service;

import dataaccess.*;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import requestsresults.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService service;
    private UserData testUser1;
    private UserData testUser2;
    private UserData testUser3;
    private UserData testUser4;
    private UserData testUser5;

    private RegisterRequest RegReqFromUserData(UserData data) {
        return new RegisterRequest(data.username(), data.password(), data.email());
    }

    private void CheckForUser1(RegisterResult result) {
        //Asserts that the result has the right username and an authToken and that "testUser1" is in the userDAO
        assertNotNull(userDAO.getUser("testUser1"));
        assertEquals("testUser1", result.username());
        assertNotNull(result.authToken());
    }

    private void CheckForAuth(String authToken, String username){
        //Asserts that the authDAO has an authorization tied to authToken and that it is associated with username
        AuthData authData = authDAO.getAuth(authToken);
        assertNotNull(authData);
        assertEquals(username, authData.user());
    }

    @BeforeAll
    static void makeUsers() {
        UserData testUser1 = new UserData("testUser1", "superSecure", "test@test.test");
        UserData testUser2 = new UserData("testUser2", "superSecure", "test@test.test");
        UserData testUser3 = new UserData("testUser3", "superSecure", "test@test.test");
        UserData testUser4 = new UserData("testUser4", "superSecure", "test@test.test");
        UserData testUser5 = new UserData("testUser5", "superSecure", "test@test.test");
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
        CheckForUser1(result);
    }

    @Test
    @DisplayName("Successful Registration - Occupied DAO")
    void successBusyRegister() throws ResponseException{
        userDAO.createUser(testUser2);
        userDAO.createUser(testUser3);
        userDAO.createUser(testUser4);
        userDAO.createUser(testUser5);
        RegisterResult result = service.register(RegReqFromUserData(testUser1));
        CheckForUser1(result);
    }

    @Test
    @DisplayName("Register Username Already In Use")
    void registerTaken() {

    }

    @Test
    @DisplayName("Incomplete Registration")
    void registerIncomplete() {

    }

    @Test
    void login() {
    }

    @Test
    void logout() {
    }
}