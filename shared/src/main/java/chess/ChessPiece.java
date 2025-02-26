package chess;

import java.util.Collection;
import java.util.Objects;
import java.util.HashSet;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        myColor = pieceColor;
        myType = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return myColor == that.myColor && myType == that.myType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(myColor, myType);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return myColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return myType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (myType) {
            case KING -> kingMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case BISHOP -> bishopMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    private boolean canMoveTo(ChessBoard board, ChessPosition destination) {
        //Returns true if destination on board is empty or contains a piece of the other color.
        //Returns false if position is not on board or contains a friendly piece
        //Doesn't check if the piece can legally get there, that logic will be handled by pieceMoves
        //Doesn't work for pawns
        int row = destination.getRow();
        int col = destination.getColumn();
        if (!inBounds(row, col)) {
            return false;
        }
        if (board.getPiece(destination) != null) {
            //Space contains a piece
            //Return true if this.myColor is different from that piece's color
            return myColor != board.getPiece(destination).getTeamColor();
        }
        else {
            //Space is empty
            return true;
        }
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition destination;
        HashSet<ChessMove> legalMoves = new HashSet<ChessMove>() {
        };
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        if (row != 1) {
            //Check down-left
            if (col != 1) {
                destination = new ChessPosition(row - 1, col - 1);
                if (this.canMoveTo(board, destination)) {
                    legalMoves.add(new ChessMove(myPosition, destination, null));
                }
            }
            //Check down
            destination = new ChessPosition(row - 1, col);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
            }
            //Check down-right
            if (col != 8) {
                destination = new ChessPosition(row - 1, col + 1);
                if (this.canMoveTo(board, destination)) {
                    legalMoves.add(new ChessMove(myPosition, destination, null));
                }
            }
        }
        if (row != 8) {
            //Check up-left
            if (col != 1) {
                destination = new ChessPosition(row + 1, col - 1);
                if (this.canMoveTo(board, destination)) {
                    legalMoves.add(new ChessMove(myPosition, destination, null));
                }
            }
            //Check up
            destination = new ChessPosition(row + 1, col);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
            }
            //Check up-right
            if(col != 8) {
                destination = new ChessPosition(row + 1, col + 1);
                if (this.canMoveTo(board, destination)) {
                    legalMoves.add(new ChessMove(myPosition, destination, null));
                }
            }
        }
        if (col != 1) {
            //Check left
            destination = new ChessPosition(row, col - 1);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
            }
        }
        if (col != 8) {
            //Check right
            destination = new ChessPosition(row, col + 1);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
            }
        }
        return legalMoves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> legalMoves = new HashSet<>();
        //Check up-left diagonal
        linearMoves(board, myPosition, 1, -1, legalMoves);
        //Check up-right diagonal
        linearMoves(board, myPosition, 1, 1, legalMoves);
        //Check down-right diagonal
        linearMoves(board, myPosition, -1, 1, legalMoves);
        //Check down-left diagonal
        linearMoves(board, myPosition, -1, -1, legalMoves);
        //Check up moves
        linearMoves(board, myPosition, 1, 0, legalMoves);
        //Check down moves
        linearMoves(board, myPosition, -1, 0, legalMoves);
        //Check left moves
        linearMoves(board, myPosition, 0, -1, legalMoves);
        //Check right moves
        linearMoves(board, myPosition, 0, 1, legalMoves);
        return legalMoves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> legalMoves = new HashSet<>();
        //Check up-left diagonal
        linearMoves(board, myPosition, 1, -1, legalMoves);
        //Check up-right diagonal
        linearMoves(board, myPosition, 1, 1, legalMoves);
        //Check down-right diagonal
        linearMoves(board, myPosition, -1, 1, legalMoves);
        //Check down-left diagonal
        linearMoves(board, myPosition, -1, -1, legalMoves);
        return legalMoves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> legalMoves = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition destination;
        //Left 2 up 1
        destination = new ChessPosition(row + 1, col - 2);
        if (this.canMoveTo(board, destination)) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        //Left 1 up 2
        destination = new ChessPosition(row + 2, col - 1);
        if (this.canMoveTo(board, destination)) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        //Right 1 up 2
        destination = new ChessPosition(row + 2, col + 1);
        if (this.canMoveTo(board, destination)) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        //Right 2 up 1
        destination = new ChessPosition(row + 1, col + 2);
        if (this.canMoveTo(board, destination)) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }

        //Right 2 down 1
        destination = new ChessPosition(row - 1, col + 2);
        if (this.canMoveTo(board, destination)) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        //Right 1 down 2
        destination = new ChessPosition(row - 2, col + 1);
        if (this.canMoveTo(board, destination)) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        //Left 1 down 2
        destination = new ChessPosition(row - 2, col - 1);
        if (this.canMoveTo(board, destination)) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        //Left 2 down 1
        destination = new ChessPosition(row - 1, col - 2);
        if (this.canMoveTo(board, destination)) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        return legalMoves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> legalMoves = new HashSet<>();
        //Check up moves
        linearMoves(board, myPosition, 1, 0, legalMoves);
        //Check down moves
        linearMoves(board, myPosition, -1, 0, legalMoves);
        //Check left moves
        linearMoves(board, myPosition, 0, -1, legalMoves);
        //Check right moves
        linearMoves(board, myPosition, 0, 1, legalMoves);
        return legalMoves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        //Cannot use the ChessPiece.canMoveTo method because of pawn special rules
        return switch (myColor) {
            case WHITE -> whitePawnMoves(board, myPosition);
            case BLACK -> blackPawnMoves(board, myPosition);
        };

    }

    private Collection<ChessMove> whitePawnMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> legalMoves = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition destination;
        //If row == 8, pawn cannot move
        if (row == 8) {
            return legalMoves;
        }
        //Basic move forward (non-promotion)
        destination = new ChessPosition(row + 1, col);
        if (row < 7 & board.getPiece(destination) == null) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        //Basic move forward (with promotion)
        if (row == 7 & board.getPiece(destination) == null) {
            pawnPromotionMoves(myPosition, destination, legalMoves);
        }
        //Basic Captures (non-promotion)
        destination = new ChessPosition(row + 1, col - 1);
        //Must not be on left-most column, not be one row below top, and space must be occupied by an enemy piece.
        if (col != 1 & row < 7 & board.getPiece(destination) != null) {
            if (board.getPiece(destination).getTeamColor() == ChessGame.TeamColor.BLACK) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
            }
        }
        destination = new ChessPosition(row + 1, col + 1);
        //Must not be on right-most column, not be one row below top, and space must be occupied by an enemy piece.
        if (col != 8 & row < 7 & board.getPiece(destination) != null) {
            if (board.getPiece(destination).getTeamColor() == ChessGame.TeamColor.BLACK) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
            }
        }
        //Basic Captures (with promotion)
        destination = new ChessPosition(row + 1, col - 1);
        //Must not be on left-most column, be one row below top, and space must be occupied by an enemy piece.
        if (col != 1 & row == 7 & board.getPiece(destination) != null) {
            if (board.getPiece(destination).getTeamColor() == ChessGame.TeamColor.BLACK) {
                pawnPromotionMoves(myPosition, destination, legalMoves);
            }
        }
        destination = new ChessPosition(row + 1, col + 1);
        //Must not be on right-most column, be one row below top, and space must be occupied by an enemy piece.
        if (col != 8 & row == 7 & board.getPiece(destination) != null) {
            if (board.getPiece(destination).getTeamColor() == ChessGame.TeamColor.BLACK) {
                pawnPromotionMoves(myPosition, destination, legalMoves);
            }
        }
        //Initial double-move (can only be performed from row 2, will never promote). Both spaces ahead must be empty.
        //Option of just advancing by 1 space handled above
        destination = new ChessPosition(row + 2, col);
        if (row == 2 & board.getPiece(new ChessPosition(row + 1, col)) == null & board.getPiece(destination) == null) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        return legalMoves;
    }

    private Collection<ChessMove> blackPawnMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> legalMoves = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition destination;
        //If row == 1, pawn cannot move
        if (row == 1) {
            return legalMoves;
        }
        //Basic move forward (non-promotion)
        destination = new ChessPosition(row - 1, col);
        if (row > 2 & board.getPiece(destination) == null) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        //Basic move forward (with promotion)
        if (row == 2 & board.getPiece(destination) == null) {
            pawnPromotionMoves(myPosition, destination, legalMoves);
        }
        //Basic Captures (non-promotion)
        destination = new ChessPosition(row - 1, col - 1);
        //Must not be on left-most column, not be one row above bottom, and space must be occupied by an enemy piece.
        if (col != 1 & row > 2 & board.getPiece(destination) != null) {
            if (board.getPiece(destination).getTeamColor() == ChessGame.TeamColor.WHITE) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
            }
        }
        destination = new ChessPosition(row - 1, col + 1);
        //Must not be on right-most column, not be one row above bottom, and space must be occupied by an enemy piece.
        if (col != 8 & row > 2 & board.getPiece(destination) != null) {
            if (board.getPiece(destination).getTeamColor() == ChessGame.TeamColor.WHITE) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
            }
        }
        //Basic Captures (with promotion)
        destination = new ChessPosition(row - 1, col - 1);
        //Must not be on left-most column, be one row above bottom, and space must be occupied by an enemy piece.
        if (col != 1 & row == 2 & board.getPiece(destination) != null) {
            if (board.getPiece(destination).getTeamColor() == ChessGame.TeamColor.WHITE) {
                pawnPromotionMoves(myPosition, destination, legalMoves);
            }
        }
        destination = new ChessPosition(row - 1, col + 1);
        //Must not be on right-most column, be one row above bottom, and space must be occupied by an enemy piece.
        if (col != 8 & row == 2 & board.getPiece(destination) != null) {
            if (board.getPiece(destination).getTeamColor() == ChessGame.TeamColor.WHITE) {
                pawnPromotionMoves(myPosition, destination, legalMoves);
            }
        }
        //Initial double-move (can only be performed from row 7, will never promote). Both spaces ahead must be empty.
        //Option of just advancing by 1 space handled above
        destination = new ChessPosition(row - 2, col);
        if (row == 7 & board.getPiece(new ChessPosition(row - 1, col)) == null & board.getPiece(destination) == null) {
            legalMoves.add(new ChessMove(myPosition, destination, null));
        }
        return legalMoves;
    }

    private void linearMoves(ChessBoard board, ChessPosition myPosition, int rChange, int cChange, Collection<ChessMove> legalMoves) {
        //rChange is change in row: 1 for up, -1 for down, 0 to stay in same row
        //cChange is change in column: 1 for right, -1 for left, 0 to stay in same column
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition destination;
        row += rChange;
        col += cChange;
        while (inBounds(row, col)) {
            destination = new ChessPosition(row, col);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                row = row + rChange;
                col = col + cChange;
            } else {
                break;
            }
        }
    }

    private boolean inBounds(int row, int col) {
        if (row < 1 || row > 8) {
            return false;
        }
        if (col < 1 || col > 8) {
            return false;
        }
        return true;
    }

    private void pawnPromotionMoves(ChessPosition myPosition, ChessPosition destination, Collection<ChessMove> legalMoves) {
        legalMoves.add(new ChessMove(myPosition, destination, PieceType.QUEEN));
        legalMoves.add(new ChessMove(myPosition, destination, PieceType.ROOK));
        legalMoves.add(new ChessMove(myPosition, destination, PieceType.BISHOP));
        legalMoves.add(new ChessMove(myPosition, destination, PieceType.KNIGHT));
    }

    private final ChessGame.TeamColor myColor;
    private final ChessPiece.PieceType myType;
}
