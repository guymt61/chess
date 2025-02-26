package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    public ChessGame() {
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        currentTurn = ChessGame.TeamColor.WHITE;
        lastMove = null;
        lastMovingPiece = null;
        whiteKingMoved = false;
        blackKingMoved = false;
        rook11Moved = false;
        rook18Moved = false;
        rook81Moved = false;
        rook88Moved = false;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(gameBoard, chessGame.gameBoard) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameBoard, currentTurn);
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
        ChessPiece movingPiece = gameBoard.getPiece(startPosition);
        if (movingPiece == null) {
            return null;
        }
        HashSet<ChessMove> legalMoves = new HashSet<>();
        Collection<ChessMove> proposedMoves = movingPiece.pieceMoves(gameBoard, startPosition);
        for (Iterator<ChessMove> moveIt = proposedMoves.iterator(); moveIt.hasNext();) {
            ChessMove move = moveIt.next();
            ChessPosition destination = move.getEndPosition();
            ChessPiece aboutToGetCaptured = gameBoard.getPiece(destination);
            this.forceMove(move);
            if (!isInCheck(movingPiece.getTeamColor())) {
                legalMoves.add(move);
            }
            this.forceMove(new ChessMove(destination, startPosition, null));
            gameBoard.addPiece(destination, aboutToGetCaptured);
        }
        //Check for En Passant
        checkForBlackEnPassant(startPosition, legalMoves);
        checkForWhiteEnPassant(startPosition, legalMoves);
        //Check for castling
        if (startPosition.getRow() == 1 && startPosition.getColumn() == 5) {
            checkForWhiteCastle(legalMoves);
        }
        if (startPosition.getRow() == 8 && startPosition.getColumn() == 5) {
            checkForBlackCastle(legalMoves);
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece movingPiece = gameBoard.getPiece(startPosition);
        if (movingPiece == null) {
            throw new InvalidMoveException("Invalid move: no piece at given start position");
        }
        if (!validMoves(startPosition).contains(move)) {
            throw new InvalidMoveException("Invalid move entered");
        }
        if (movingPiece.getTeamColor() != this.getTeamTurn()) {
            throw new InvalidMoveException("Invalid move: wrong turn");
        }
        if (move.getPromotionPiece() == null) {
            //If the move is En Passant, get rid of the captured pawn
            if (gameBoard.getPiece(endPosition) == null && movingPiece.getPieceType() == ChessPiece.PieceType.PAWN && startPosition.getColumn() != endPosition.getColumn()) {
                gameBoard.addPiece(new ChessPosition(startPosition.getRow(), endPosition.getColumn()), null);
            }
            gameBoard.addPiece(endPosition, movingPiece);
            gameBoard.addPiece(startPosition, null);
            //If the move is a Castle, move the rook into place
            if (movingPiece.getPieceType() == ChessPiece.PieceType.KING && startPosition.getRow() == 1 && startPosition.getColumn() == 5 && endPosition.getColumn() == 3 ) {
                //White castle left
                ChessMove rookJump = new ChessMove(new ChessPosition(1, 1), new ChessPosition(1, 4), null);
                forceMove(rookJump);
            }
            if (movingPiece.getPieceType() == ChessPiece.PieceType.KING && startPosition.getRow() == 1 && startPosition.getColumn() == 5 && endPosition.getColumn() == 7 ) {
                //White castle right
                ChessMove rookJump = new ChessMove(new ChessPosition(1, 8), new ChessPosition(1, 6), null);
                forceMove(rookJump);
            }
            if (movingPiece.getPieceType() == ChessPiece.PieceType.KING && startPosition.getRow() == 8 && startPosition.getColumn() == 5 && endPosition.getColumn() == 3 ) {
                //Black castle left
                ChessMove rookJump = new ChessMove(new ChessPosition(8, 1), new ChessPosition(8, 4), null);
                forceMove(rookJump);
            }
            if (movingPiece.getPieceType() == ChessPiece.PieceType.KING && startPosition.getRow() == 8 && startPosition.getColumn() == 5 && endPosition.getColumn() == 7 ) {
                //Black castle right
                ChessMove rookJump = new ChessMove(new ChessPosition(8, 8), new ChessPosition(8, 6), null);
                forceMove(rookJump);
            }

        }
        else {
            ChessPiece promotedPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
            gameBoard.addPiece(endPosition, promotedPiece);
            gameBoard.addPiece(startPosition, null);
        }
        if (this.getTeamTurn() == TeamColor.WHITE) {
            this.setTeamTurn(TeamColor.BLACK);
        }
        else {
            this.setTeamTurn(TeamColor.WHITE);
        }
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING) {
            if (!whiteKingMoved && movingPiece.getTeamColor() == TeamColor.WHITE) {
                whiteKingMoved = true;
            }
            if (!blackKingMoved && movingPiece.getTeamColor() == TeamColor.BLACK) {
                blackKingMoved = true;
            }
        }
        if (movingPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (startPosition.getRow() == 1 && startPosition.getColumn() == 1) {
                rook11Moved = true;
            }
            if (startPosition.getRow() == 1 && startPosition.getColumn() == 8) {
                rook18Moved = true;
            }
            if (startPosition.getRow() == 8 && startPosition.getColumn() == 1) {
                rook81Moved = true;
            }
            if (startPosition.getRow() == 8 && startPosition.getColumn() == 8) {
                rook11Moved = true;
            }
        }
        lastMove = move;
        lastMovingPiece = movingPiece;
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
        if (!isInCheck(teamColor)) {
            return false;
        }
        else {
            return getWholeTeamMoves(teamColor).isEmpty();
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        else {
            return getWholeTeamMoves(teamColor).isEmpty();
        }
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.gameBoard = board;
        lastMove = null;
        lastMovingPiece = null;
        whiteKingMoved = false;
        blackKingMoved = false;
        rook11Moved = false;
        rook18Moved = false;
        rook81Moved = false;
        rook88Moved = false;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.gameBoard;
    }

    /**
        A private callable method to make a move whether it's valid or not
        Does not handle pawn promotions, use makeMove for that
     */
    private void forceMove(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece movingPiece = gameBoard.getPiece(startPosition);
        gameBoard.addPiece(endPosition, movingPiece);
        gameBoard.addPiece(startPosition, null);
    }

    /**
     * Returns a collection of all legal moves for a team
     */
    private Collection<ChessMove> getWholeTeamMoves(ChessGame.TeamColor teamColor) {
        HashSet<ChessMove> allLegalMoves = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition targetPosition = new ChessPosition(i, j);
                ChessPiece targetPiece = this.gameBoard.getPiece(targetPosition);
                if (targetPiece == null) {
                    continue;
                }
                if (targetPiece.getTeamColor() == teamColor) {
                    Collection<ChessMove> targetPieceMoves = validMoves(targetPosition);
                    allLegalMoves.addAll(targetPieceMoves);
                }
            }
        }
        return allLegalMoves;
    }

    private void checkForBlackCastle(Collection<ChessMove> legalMoves) {
        ChessPosition startPosition = new ChessPosition(8, 5);
        ChessPiece movingPiece = gameBoard.getPiece(startPosition);
        if (movingPiece.getPieceType() == null) {
            return;
        }
        if (movingPiece.getPieceType() != ChessPiece.PieceType.KING) {
            return;
        }
        if (movingPiece.getTeamColor() != TeamColor.BLACK || blackKingMoved) {
            return;
        }
        //Check for left-side castling
        //Rook could have been captured without moving, so must verify it's still there
        if (gameBoard.getPiece(new ChessPosition(8, 1)) != null && !rook81Moved) {
            ChessPiece castlePartner = gameBoard.getPiece(new ChessPosition(8, 1));
            if (castlePartner.getPieceType() == ChessPiece.PieceType.ROOK && castlePartner.getTeamColor() == TeamColor.BLACK) {
                //Make sure (8,4) and (8,3) are empty
                castleHelper(startPosition, new ChessPosition(8, 4), new ChessPosition(8, 3), legalMoves);
            }
        }
        //Check for right-side castling
        //Rook could have been captured without moving, so must verify it's still there
        if (gameBoard.getPiece(new ChessPosition(8, 8)) != null && !rook88Moved) {
            ChessPiece castlePartner = gameBoard.getPiece(new ChessPosition(8, 8));
            if (castlePartner.getPieceType() == ChessPiece.PieceType.ROOK && castlePartner.getTeamColor() == TeamColor.BLACK) {
                //Make sure (8,6) and (8,7) are empty
                castleHelper(startPosition, new ChessPosition(8, 6), new ChessPosition(8, 7), legalMoves);
            }
        }
    }

    private void checkForWhiteCastle(Collection<ChessMove> legalMoves) {
        ChessPosition startPosition = new ChessPosition(1, 5);
        ChessPiece movingPiece = gameBoard.getPiece(startPosition);
        if (movingPiece.getPieceType() == null) {
            return;
        }
        if (movingPiece.getPieceType() != ChessPiece.PieceType.KING) {
            return;
        }
        if (movingPiece.getTeamColor() != TeamColor.WHITE || whiteKingMoved) {
            return;
        }
        //Check for left-side castling
        //Rook could have been captured without moving, so must verify it's still there
        if (gameBoard.getPiece(new ChessPosition(1, 1)) != null && !rook11Moved) {
            ChessPiece castlePartner = gameBoard.getPiece(new ChessPosition(1, 1));
            if (castlePartner.getPieceType() == ChessPiece.PieceType.ROOK && castlePartner.getTeamColor() == TeamColor.WHITE) {
                //Make sure (1,4) and (1,3) are empty
                castleHelper(startPosition, new ChessPosition(1, 4), new ChessPosition(1, 3), legalMoves);
            }
        }
        //Check for right-side castling
        //Rook could have been captured without moving, so must verify it's still there
        if (gameBoard.getPiece(new ChessPosition(1, 8)) != null && !rook18Moved) {
            ChessPiece castlePartner = gameBoard.getPiece(new ChessPosition(1, 8));
            if (castlePartner.getPieceType() == ChessPiece.PieceType.ROOK && castlePartner.getTeamColor() == TeamColor.WHITE) {
                //Make sure (1,6) and (1,7) are empty
                castleHelper(startPosition, new ChessPosition(1, 6), new ChessPosition(1, 7), legalMoves);
            }
        }
    }

    private void castleHelper(ChessPosition startPosition, ChessPosition step1, ChessPosition step2, Collection<ChessMove> legalMoves) {
        //Ensures that the castling move isn't into, out of, or through check
        TeamColor activeColor = gameBoard.getPiece(startPosition).getTeamColor();
        if (isInCheck(activeColor)) {
            return;
        }
        if (gameBoard.getPiece(step1) == null && gameBoard.getPiece(step2) == null) {
            boolean safe = true;
            forceMove(new ChessMove(startPosition, step1, null));
            if (isInCheck(activeColor)) {
                safe = false;
            }
            forceMove(new ChessMove(step1, step2, null));
            if (isInCheck(activeColor)) {
                safe = false;
            }
            if (safe) {
                legalMoves.add(new ChessMove(startPosition, step2, null));
            }
            forceMove(new ChessMove(step2, startPosition, null));
        }
    }

    private boolean lastMoveWhitePawnDouble() {
        if (lastMovingPiece == null) {
            return false;
        }
        if (lastMovingPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        }
        //Checking for color is unnecessary, since black pawns cannot make the move below
        return (lastMove.getStartPosition().getRow() == 2 && lastMove.getEndPosition().getRow() == 4);
    }

    private boolean lastMoveBlackPawnDouble() {
        if (lastMovingPiece == null) {
            return false;
        }
        if (lastMovingPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        }
        //Checking for color is unnecessary, since black pawns cannot make the move below
        return (lastMove.getStartPosition().getRow() == 7 && lastMove.getEndPosition().getRow() == 5);
    }

    private void checkForWhiteEnPassant(ChessPosition startPosition, Collection<ChessMove> legalMoves) {
        ChessPiece movingPiece = gameBoard.getPiece(startPosition);
        int row = startPosition.getRow();
        int col = startPosition.getColumn();
        if (movingPiece.getPieceType() != ChessPiece.PieceType.PAWN || movingPiece.getTeamColor() != TeamColor.WHITE) {
            return;
        }
        if (!lastMoveBlackPawnDouble()) {
            return;
        }
        ChessPosition victim = lastMove.getEndPosition();
        if (Math.abs(victim.getColumn() - col) != 1 || row != 5) {
            return;
        }
        //En passant on a black piece will be up and to the side
        ChessPosition destination = new ChessPosition(6, victim.getColumn());
        ChessMove enPassant = new ChessMove(startPosition, destination, null);
        forceMove(enPassant);
        gameBoard.addPiece(lastMove.getEndPosition(), null);
        if (!isInCheck(movingPiece.getTeamColor())) {
            legalMoves.add(enPassant);
        }
        forceMove(new ChessMove(destination, startPosition, null));
        gameBoard.addPiece(lastMove.getEndPosition(),lastMovingPiece);
    }

    private void checkForBlackEnPassant(ChessPosition startPosition, Collection<ChessMove> legalMoves) {
        ChessPiece movingPiece = gameBoard.getPiece(startPosition);
        int row = startPosition.getRow();
        int col = startPosition.getColumn();
        if (movingPiece.getPieceType() != ChessPiece.PieceType.PAWN || movingPiece.getTeamColor() != TeamColor.BLACK) {
            return;
        }
        if (!lastMoveWhitePawnDouble()) {
            return;
        }
        ChessPosition victim = lastMove.getEndPosition();
        if (Math.abs(victim.getColumn() - col) != 1 || row != 4) {
            return;
        }
        //En passant on a white piece will be up and to the side
        ChessPosition destination = new ChessPosition(3, victim.getColumn());
        ChessMove enPassant = new ChessMove(startPosition, destination, null);
        forceMove(enPassant);
        gameBoard.addPiece(lastMove.getEndPosition(), null);
        if (!isInCheck(movingPiece.getTeamColor())) {
            legalMoves.add(enPassant);
        }
        forceMove(new ChessMove(destination, startPosition, null));
        gameBoard.addPiece(lastMove.getEndPosition(),lastMovingPiece);
    }

    private ChessBoard gameBoard;
    private ChessGame.TeamColor currentTurn;
    private ChessMove lastMove;
    private ChessPiece lastMovingPiece;
    private boolean whiteKingMoved;
    private boolean rook11Moved;
    private boolean rook18Moved;
    private boolean blackKingMoved;
    private boolean rook81Moved;
    private boolean rook88Moved;

    //private boolean debug = true;
}
