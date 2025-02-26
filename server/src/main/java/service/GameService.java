package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.*;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import requestsresults.*;

import java.util.HashSet;
import java.util.Random;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final Random IDs;
    private final HashSet<Integer> usedIDs;

    private void verifyAuth(String authToken) throws ResponseException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new ResponseException(401, "Error: Unauthorized");
        }
    }

    public GameService(GameDAO gameDAOToUse, AuthDAO authDAOToUse) {
        gameDAO = gameDAOToUse;
        authDAO = authDAOToUse;
        IDs = new Random();
        usedIDs = new HashSet<>();
    }

    public ListResult list(ListRequest listReq) throws ResponseException{
        verifyAuth(listReq.authToken());
        HashSet<GameData> allGames = gameDAO.listGames();
        return new ListResult(allGames);
    }

    public CreateResult create(CreateRequest createReq) throws ResponseException {
        verifyAuth(createReq.authToken());
        if (createReq.gameName() == null) {
            throw new ResponseException(400, "Error: Bad Request");
        }
        ChessGame newGame = new ChessGame();
        int ID = IDs.nextInt();
        while (usedIDs.contains(ID)) {
            ID = IDs.nextInt();
        }
        usedIDs.add(ID);
        gameDAO.createGame(new GameData(ID, null, null, createReq.gameName(), newGame));
        return new CreateResult(ID);
    }

    public JoinResult join(JoinRequest joinReq) throws ResponseException{
        return null;
    }
}
