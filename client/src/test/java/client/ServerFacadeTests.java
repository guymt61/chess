import client.ServerFacade;
import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import requestsresults.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String url = String.format("http://localhost:%s",port);
        facade = new ServerFacade(url);
    }

    @BeforeEach
    public void clearData() {
        try {
            facade.clear();
        } catch (ResponseException e) {
            System.out.println("Could not clear database: " + e.getMessage());
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @DisplayName("Good register request")
    @Order(1)
    public void goodRegister() throws ResponseException{
            RegisterResult result = facade.register("testUser1", "SuperSecure", "test@test.test");
            assertNotNull(result);
            assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("Bad register request")
    @Order(2)
    public void badRegister(){
        try {
            facade.register("NoPassword", null, "test@test.test");
            fail("Register should have thrown an error for null password");
        } catch (ResponseException e) {
            //yippee
        }
    }

    @Test
    @DisplayName("Good login request")
    @Order(3)
    public void goodLogin() throws ResponseException{
        facade.register("testUser1", "SuperSecure", "test@test.test");
        LoginResult result = facade.login("testUser1", "SuperSecure");
        assertNotNull(result);
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("Bad login request")
    @Order(4)
    public void badLogin() {
        try {
            facade.login("IDon't", "Exist");
            fail("Login should've thrown an error");
        } catch (ResponseException e) {
            //wahoo
        }
    }

    @Test
    @DisplayName("Good create request")
    @Order(5)
    public void goodCreate() throws ResponseException {
        RegisterResult registerResult = facade.register("testUser1", "SuperSecure", "test@test.test");
        String authToken = registerResult.authToken();
        CreateResult result = facade.create("testGame", authToken);
        assertNotNull(result);
        int createdID = result.gameID();
        assertTrue(createdID > 0);
    }

    @Test
    @DisplayName("Bad create request")
    @Order(6)
    public void badCreate() {
        try {
            facade.create("badGame", "fake");
            fail("Create should've thrown an error");
        } catch (ResponseException e) {
            //huzzah
        }
    }

}
