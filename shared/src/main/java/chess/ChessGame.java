package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    public ChessGame() {
        this.gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        this.currentTurn = ChessGame.TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //throw new RuntimeException("Implementation incomplete");

        if (this.gameBoard.getPiece(startPosition) == null) {
            return null;
        }
        HashSet<ChessMove> legalMoves = new HashSet<>();


        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        HashSet<ChessPosition> allUnderAttack = new HashSet<>();
        ChessPosition kingPosition = null;
        for (int i = 1; i <= 8; i++) {
            for(int j = 1; j <= 8; j++) {
                ChessPosition targetPosition = new ChessPosition(i, j);
                ChessPiece targetPiece = this.gameBoard.getPiece(targetPosition);
                if (targetPiece == null) {
                    continue;
                }
                ChessPiece.PieceType targetPieceType = targetPiece.getPieceType();
                if (targetPieceType == ChessPiece.PieceType.KING & targetPiece.getTeamColor() == teamColor) {
                    kingPosition = targetPosition;
                }
                else if (targetPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> targetPieceMoves = targetPiece.pieceMoves(gameBoard, targetPosition);
                    for (Iterator<ChessMove> moveIt = targetPieceMoves.iterator(); moveIt.hasNext();) {
                        ChessMove move = moveIt.next();
                        allUnderAttack.add(move.getEndPosition());
                    }
                }
            }
        }
        return allUnderAttack.contains(kingPosition);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.gameBoard;
    }

    private ChessBoard gameBoard;
    private ChessGame.TeamColor currentTurn;
}
