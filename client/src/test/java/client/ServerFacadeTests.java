import client.ServerFacade;
import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
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
    public void goodRegister() throws ResponseException{
            AuthData authData = facade.register("testUser1", "SuperSecure", "test@test.test");
            assertNotNull(authData);
            assertTrue(authData.authToken().length() > 10);
    }

    @Test
    @DisplayName("Bad register request")
    public void badRegister(){
        try {
            AuthData authData = facade.register("NoPassword", null, "test@test.test");
            fail("Register should have thrown an error for null password");
        } catch (ResponseException e) {
            //yippee
        }
    }

}
