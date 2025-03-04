package dataaccess;

import model.UserData;

public class MySQLUserDAO implements UserDAO {
    //Clear all user data
    public void clear() {}

    //Find userData based on username
    public UserData getUser(String username) {
        return null;
    }

    //Add a new user
    public void createUser(UserData data) {}
}
