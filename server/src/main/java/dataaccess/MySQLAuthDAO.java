package dataaccess;

import model.AuthData;

public class MySQLAuthDAO implements AuthDAO {
    //Clear all AuthData
    public void clear() {}

    //Create a new authorization
    public void createAuth(AuthData auth) {}

    //Retrieve an authorization by its authToken
    public AuthData getAuth(String authToken) {
        return null;
    }

    //Delete an authorization, invalidating it
    public void deleteAuth(AuthData auth) {}
}
