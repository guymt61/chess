package dataaccess;
import model.AuthData;

public interface AuthDAO {

    //Clear all AuthData
    void clear();

    //Create a new authorization
    void createAuth(AuthData auth);

    //Retrieve an authorization by its authToken
    AuthData getAuth(String authToken) throws DataAccessException;

    //Delete an authorization, invalidating it
    void deleteAuth(AuthData auth) throws DataAccessException;
}
