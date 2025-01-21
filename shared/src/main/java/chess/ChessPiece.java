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
        //Doesn't check if the piece can legally get there, that logic will be handled by pieceMoves
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
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition destination;
        //Check up-left diagonal
        int destRow = row + 1;
        int destCol = col - 1;
        while (destRow <= 8 & destCol >= 1) {
            destination = new ChessPosition(destRow, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow++;
                destCol--;
            } else {
                break;
            }
        }
        //Check up-right diagonal
        destRow = row + 1;
        destCol = col + 1;
        while (destRow <= 8 & destCol <= 8) {
            destination = new ChessPosition(destRow, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow++;
                destCol++;
            } else {
                break;
            }
        }
        //Check down-right diagonal
        destRow = row - 1;
        destCol = col + 1;
        while (destRow >= 1 & destCol <= 8) {
            destination = new ChessPosition(destRow, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow--;
                destCol++;
            } else {
                break;
            }
        }
        //Check down-left diagonal
        destRow = row - 1;
        destCol = col - 1;
        while (destRow >= 1 & destCol >= 1) {
            destination = new ChessPosition(destRow, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow--;
                destCol--;
            } else {
                break;
            }
        }
        //Check up moves
        destRow = row + 1;
        while (destRow <= 8) {
            destination = new ChessPosition(destRow, col);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow++;
            } else {
                break;
            }
        }
        //Check down moves
        destRow = row - 1;
        while (destRow >= 1) {
            destination = new ChessPosition(destRow, col);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow--;
            } else {
                break;
            }
        }
        //Check left moves
        destCol = col - 1;
        while (destCol >= 1) {
            destination = new ChessPosition(row, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destCol--;
            } else {
                break;
            }
        }
        //Check right moves
        destCol = col + 1;
        while (destCol <= 8) {
            destination = new ChessPosition(row, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destCol++;
            } else {
                break;
            }
        }
        return legalMoves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> legalMoves = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition destination;
        //Check up-left diagonal
        int destRow = row + 1;
        int destCol = col - 1;
        while (destRow <= 8 & destCol >= 1) {
            destination = new ChessPosition(destRow, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow++;
                destCol--;
            } else {
                break;
            }
        }
        //Check up-right diagonal
        destRow = row + 1;
        destCol = col + 1;
        while (destRow <= 8 & destCol <= 8) {
            destination = new ChessPosition(destRow, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow++;
                destCol++;
            } else {
                break;
            }
        }
        //Check down-right diagonal
        destRow = row - 1;
        destCol = col + 1;
        while (destRow >= 1 & destCol <= 8) {
            destination = new ChessPosition(destRow, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow--;
                destCol++;
            } else {
                break;
            }
        }
        //Check down-left diagonal
        destRow = row - 1;
        destCol = col - 1;
        while (destRow >= 1 & destCol >= 1) {
            destination = new ChessPosition(destRow, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow--;
                destCol--;
            } else {
                break;
            }
        }
        return legalMoves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        throw new RuntimeException("Not Implemented Yet");
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> legalMoves = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int destRow;
        int destCol;
        ChessPosition destination;
        //Check up moves
        destRow = row + 1;
        while (destRow <= 8) {
            destination = new ChessPosition(destRow, col);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow++;
            } else {
                break;
            }
        }
        //Check down moves
        destRow = row - 1;
        while (destRow >= 1) {
            destination = new ChessPosition(destRow, col);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destRow--;
            } else {
                break;
            }
        }
        //Check left moves
        destCol = col - 1;
        while (destCol >= 1) {
            destination = new ChessPosition(row, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destCol--;
            } else {
                break;
            }
        }
        //Check right moves
        destCol = col + 1;
        while (destCol <= 8) {
            destination = new ChessPosition(row, destCol);
            if (this.canMoveTo(board, destination)) {
                legalMoves.add(new ChessMove(myPosition, destination, null));
                if (board.getPiece(destination) != null) {break;}
                destCol++;
            } else {
                break;
            }
        }
        return legalMoves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        throw new RuntimeException("Not Implemented Yet");
    }

    private final ChessGame.TeamColor myColor;
    private final ChessPiece.PieceType myType;
}
