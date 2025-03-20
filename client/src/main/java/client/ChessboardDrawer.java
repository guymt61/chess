package client;
import chess.*;
import static ui.EscapeSequences.*;


public class ChessboardDrawer {
    private final ChessGame game;
    private final ChessGame.TeamColor perspective;
    private final ChessBoard board;

    private final static String LIGHT_BG = SET_BG_COLOR_LIGHT_GREY;
    private final static String DARK_BG = SET_BG_COLOR_DARK_GREY;
    private final static String lightFiller = LIGHT_BG  + EMPTY + RESET_BG_COLOR;
    private final static String darkFiller = DARK_BG + EMPTY + RESET_BG_COLOR;
    private final static String whitePovColLabels = SET_TEXT_COLOR_BLUE + "    a   b   c  d   e  f   g   h" + RESET_TEXT_COLOR;

    public ChessboardDrawer(ChessGame game, ChessGame.TeamColor pointOfView) {
        this.game = game;
        perspective = pointOfView;
        board = game.getBoard();
    }

    public String drawBoard() {
        return drawWhitePOV();
    }

    private String drawWhitePOV() {
        var output = new StringBuilder();
        output.append(RESET_TEXT_COLOR);
        output.append(whitePovColLabels);
        output.append("\n");
        //Work top down, from row 8 to row 1
        for (int i = 8; i > 0; i--) {
            if (isEven(i)) {
                //Light first
                lightPopulatedRow(output, i);
            }
            else {
                //Dark first
                darkPopulatedRow(output, i);
            }
        }
        output.append(whitePovColLabels);
        return output.toString();
    }

    private String lightEmptyRow() {
        String pair = lightFiller + darkFiller;
        return pair + pair + pair + pair;
    }

    private String darkEmptyRow() {
        String pair = darkFiller + lightFiller;
        return pair + pair + pair + pair;
    }

    private String pieceOnLight(ChessPiece piece) {
        if (piece == null) {
            return lightFiller;
        }
        String pieceIcon = getPieceIcon(piece);
        return LIGHT_BG + pieceIcon + RESET_BG_COLOR;

    }

    private String pieceOnDark(ChessPiece piece) {
        if (piece == null) {
            return darkFiller;
        }
        String pieceIcon = getPieceIcon(piece);
        return DARK_BG + pieceIcon + RESET_BG_COLOR;
    }

    private String getPieceIcon(ChessPiece piece) {
        String pieceIcon;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            pieceIcon = switch (piece.getPieceType()) {
                case PAWN -> WHITE_PAWN;
                case ROOK -> WHITE_ROOK;
                case KING -> WHITE_KING;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case QUEEN -> WHITE_QUEEN;
            };
        }
        else {
            pieceIcon = switch (piece.getPieceType()) {
                case PAWN -> BLACK_PAWN;
                case QUEEN -> BLACK_QUEEN;
                case KNIGHT -> BLACK_KNIGHT;
                case BISHOP -> BLACK_BISHOP;
                case KING -> BLACK_KING;
                case ROOK -> BLACK_ROOK;
            };
        }
        return pieceIcon;
    }

    private boolean isEven(int i) {
        return i % 2 == 0;
    }

    private void lightPopulatedRow(StringBuilder output, int row) {
        addRowStartLabel(output, row);
        for (int j = 1; j <= 8; j++) {
            //Even columns dark, odd columns light
            ChessPosition position = new ChessPosition(row, j);
            if (isEven(j)) {
                output.append(pieceOnDark(board.getPiece(position)));
            }
            else {
                output.append(pieceOnLight(board.getPiece(position)));
            }
        }
        addRowEndLabel(output, row);
        output.append("\n");
    }

    private void darkPopulatedRow(StringBuilder output, int row) {
        addRowStartLabel(output, row);
        for (int j = 1; j <= 8; j++) {
            //Even columns light, odd columns dark
            ChessPosition position = new ChessPosition(row, j);
            if (isEven(j)) {
                output.append(pieceOnLight(board.getPiece(position)));
            }
            else {
                output.append(pieceOnDark(board.getPiece(position)));
            }
        }
        addRowEndLabel(output, row);
        output.append("\n");
    }

    private void addRowStartLabel(StringBuilder output, int row) {
        output.append(SET_TEXT_COLOR_BLUE);
        output.append(row);
        output.append(RESET_TEXT_COLOR);
        output.append("  ");
    }

    private void addRowEndLabel(StringBuilder output, int row) {
        output.append(SET_TEXT_COLOR_BLUE);
        output.append("  ");
        output.append(row);
        output.append(RESET_TEXT_COLOR);
    }
}
