package service;

import dataaccess.DataAccessException;
import dataaccess.*;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import requestsresults.*;

public class GameService {
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameService(GameDAO gameDAOToUse, AuthDAO authDAOToUse) {
        gameDAO = gameDAOToUse;
        authDAO = authDAOToUse;
    }

    public ListResult list(ListRequest listReq) throws ResponseException{
        return null;
    }

    public CreateResult create(CreateRequest createReq) throws ResponseException {
        return null;
    }

    public JoinResult join(JoinRequest joinReq) throws ResponseException{
        return null;
    }
}
