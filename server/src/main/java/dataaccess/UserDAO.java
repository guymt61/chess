package dataaccess;

import model.UserData;

public interface UserDAO {
    //Clear all user data
    void clear();

    //Find userData based on username
    UserData getUser(String username) throws DataAccessException;

    //Add a new user
    void createUser(UserData data) throws DataAccessException;
}
