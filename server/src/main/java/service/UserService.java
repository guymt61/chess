package service;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import requestsresults.*;
import java.util.UUID;

public class UserService {

    private final UserDAO DAO;

    public UserService(UserDAO DAOToUse) {
        DAO = DAOToUse;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        if (DAO.getUser(registerRequest.username()) == null) {
            UserData newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
            String newAuthToken = UUID.randomUUID().toString();
            AuthData newAuth = new AuthData(newAuthToken, registerRequest.username());
            return new RegisterResult(registerRequest.username(), newAuthToken);
        }
        else {
            //There was a problem, needs handling
            return null;
        }
    }

}
