package dataaccess;
import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {

    public MemoryUserDAO() {
        users = new HashMap<String, UserData>();
    }

    public void clear() {
        users.clear();
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void createUser(UserData data) {
        users.put(data.username(), data);
    }

    private final HashMap<String, UserData> users;
}
