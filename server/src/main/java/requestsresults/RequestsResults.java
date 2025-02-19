package requestsresults;
import java.util.Collection;
import model.GameData;


record RegisterRequest(String username, String password, String email) {}

record RegisterResult(String username, String authToken) {}

record LoginRequest(String username, String password) {}

record LoginResult(String username, String authToken) {}

record LogoutRequest(String authToken) {}

record LogoutResult() {}

record ListRequest(String authToken) {}

record ListResult(Collection<GameData> games) {}

record CreateRequest(String authToken, String gameName) {}

record CreateResult(int gameID) {}

record JoinRequest(String authToken, String playerColor, int gameID) {}

record JoinResult() {}