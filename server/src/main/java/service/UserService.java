package service;
import dataaccess.UserDAO;

public class UserService {

    private final UserDAO DAO;

    public UserService(UserDAO DAOToUse) {
        DAO = DAOToUse;
    }
}
