package dataaccess;
import model.AuthData;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {

    public MemoryAuthDAO() {
        auths = new HashMap<>();
    }

    public void clear() {
        auths.clear();
    }

    public void createAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    public void deleteAuth(AuthData auth) {
        auths.put(auth.authToken(), null);
    }

    private final HashMap<String, AuthData> auths;
}
