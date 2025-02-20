package dataaccess;
import model.AuthData;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {

    public MemoryAuthDAO() {
        Auths = new HashMap<>();
    }

    public void clear() {
        Auths.clear();
    }

    public void createAuth(AuthData auth) {
        Auths.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) {
        return Auths.get(authToken);
    }

    public void deleteAuth(AuthData auth) {
        Auths.put(auth.authToken(), null);
    }

    private final HashMap<String, AuthData> Auths;
}
