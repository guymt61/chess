package client;
import chess.*;


public class ChessboardDrawer {
    private ChessGame game;
    private ChessGame.TeamColor perspective;

    public ChessboardDrawer(ChessGame game, ChessGame.TeamColor pointOfView) {
        this.game = game;
        perspective = pointOfView;
    }

    public String drawBoard() {
        return "I'm a chess board!";
    }
}
