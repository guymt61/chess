package service;
import dataaccess.DataAccessException;
import dataaccess.*;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import requestsresults.*;
import java.util.UUID;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAOToUse, AuthDAO authDAOToUse) {
        userDAO = userDAOToUse;
        authDAO = authDAOToUse;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws ResponseException, DataAccessException {
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new ResponseException(400, "Error: bad request");
        }
        if (userDAO.getUser(registerRequest.username()) == null) {
            UserData newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
            userDAO.createUser(newUser);
            String newAuthToken = UUID.randomUUID().toString();
            AuthData newAuth = new AuthData(newAuthToken, registerRequest.username());
            authDAO.createAuth(newAuth);
            return new RegisterResult(registerRequest.username(), newAuthToken);
        }
        else {
            throw new ResponseException(403, "Error: Already Taken");
        }
    }

}
