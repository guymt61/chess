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

    @Test
    @DisplayName("Good list request")
    @Order(7)
    public void goodList() throws ResponseException {
        RegisterResult registerResult = facade.register("testUser1", "SuperSecure", "test@test.test");
        String authToken = registerResult.authToken();
        facade.create("game1", authToken);
        ListResult listResult = facade.list(authToken);
        assertFalse(listResult.games().isEmpty());
        assertEquals(1, listResult.games().size());
    }



    //@Test
    //@DisplayName("Debug: are all requests correct")
    public void checkRequests() throws ResponseException {
        System.out.println("Join: /game, PUT");
        try {facade.join(null, 0, null);} catch (ResponseException e){}
        System.out.println("Login: /session, POST");
        try {facade.login(null, null);} catch (ResponseException e){}
        System.out.println("Register: /session, POST");
        try {facade.register(null, null, null);} catch (ResponseException e){}
        System.out.println("Clear: /db, DELETE");
        try {facade.clear();} catch (ResponseException e){}
        System.out.println("Logout: /session, DELETE");
        try {facade.logout(null);} catch (ResponseException e){}
        System.out.println("List: /game, GET");
        try {facade.list(null);} catch (ResponseException e){}
        System.out.println("Create: /game, POST");
        try {facade.create(null, null);} catch (ResponseException e){}
    }

}
