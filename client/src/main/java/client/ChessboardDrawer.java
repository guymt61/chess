package client;
import chess.*;
import static ui.EscapeSequences.*;


public class ChessboardDrawer {
    private final ChessGame game;
    private final ChessGame.TeamColor perspective;
    private final ChessBoard board;

    private final static String LIGHT_BG = setColor(false, 214, 181, 73);
    private final static String DARK_BG = setColor(false, 191, 6, 6);
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
            pieceIcon = SET_TEXT_COLOR_WHITE;
        }
        else {
            pieceIcon = SET_TEXT_COLOR_BLACK;
        }
        pieceIcon += switch (piece.getPieceType()) {
                case PAWN -> BLACK_PAWN;
                case ROOK -> BLACK_ROOK;
                case KING -> BLACK_KING;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case QUEEN -> BLACK_QUEEN;
        };
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

    /**
     * Creates an escape sequence string that, if printed, should set the console text or background to any RGB color
     *
     * @param text  true if setting text color, false if setting background color
     * @param red   amount of red 0-255
     * @param green amount of green 0-255
     * @param blue  amount of blue 0-255
     * @return an escape sequence string that would set the console text or background to the specified color
     */
    private static String setColor(boolean text, int red, int green, int blue) {
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
            throw new IllegalArgumentException("Colors must be between 0 - 255");
        }
        return String.format("\u001B[%s8;2;%d;%d;%dm", (text) ? "3" : "4", red, green, blue);
    }
}
