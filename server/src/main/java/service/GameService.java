package service;

import dataaccess.DataAccessException;
import dataaccess.*;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import requestsresults.*;

import java.util.HashSet;

public class GameService {
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    private void verifyAuth(String authToken) throws ResponseException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
    }

    public GameService(GameDAO gameDAOToUse, AuthDAO authDAOToUse) {
        gameDAO = gameDAOToUse;
        authDAO = authDAOToUse;
    }

    public ListResult list(ListRequest listReq) throws ResponseException{
        verifyAuth(listReq.authToken());
        HashSet<GameData> allGames = gameDAO.listGames();
        return new ListResult(allGames);
    }

    public CreateResult create(CreateRequest createReq) throws ResponseException {
        return null;
    }

    public JoinResult join(JoinRequest joinReq) throws ResponseException{
        return null;
    }
}
