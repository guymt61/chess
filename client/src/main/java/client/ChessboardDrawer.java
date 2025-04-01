package client;
import chess.*;

import java.util.HashSet;

import static ui.EscapeSequences.*;


public class ChessboardDrawer {
    private final ChessGame game;
    private final ChessGame.TeamColor perspective;
    private final ChessBoard board;

    private final static String LIGHT_BG = setColor(false, 214, 181, 73);
    private final static String DARK_BG = setColor(false, 191, 6, 6);
    private final static String LIGHT_HIGHLIGHT_BG = SET_BG_COLOR_GREEN;
    private final static String DARK_HIGHLIGHT_BG = SET_BG_COLOR_DARK_GREEN;
    private final static String LIGHT_FILLER = LIGHT_BG  + EMPTY + RESET_BG_COLOR;
    private final static String DARK_FILLER = DARK_BG + EMPTY + RESET_BG_COLOR;
    private final static String LIGHT_HIGHLIGHT_FILLER = LIGHT_HIGHLIGHT_BG + EMPTY + RESET_BG_COLOR;
    private final static String DARK_HIGHLIGHT_FILLER = DARK_HIGHLIGHT_BG + EMPTY + RESET_BG_COLOR;
    private final static String WHITE_COLUMN_LABELS = SET_TEXT_COLOR_BLUE + "    a   b   c  d   e  f   g   h" + RESET_TEXT_COLOR;
    private final static String BLACK_COLUMN_LABELS = SET_TEXT_COLOR_BLUE + "    h   g   f  e   d  c   b   a" + RESET_TEXT_COLOR;

    public ChessboardDrawer(ChessGame game, ChessGame.TeamColor pointOfView) {
        this.game = game;
        perspective = pointOfView;
        board = game.getBoard();
    }

    public String drawBoard() {
        return switch (perspective) {
            case WHITE -> drawWhitePOV();
            case BLACK -> drawBlackPOV();
        };
    }

    public String drawWithHighlight(ChessPosition startPosition) {
        return switch (perspective) {
            case WHITE -> drawWhitePOVHighlighted(startPosition);
            case BLACK -> drawBlackPOVHighlighted(startPosition);
        };
    }

    private String drawWhitePOV() {
        var output = new StringBuilder();
        output.append(RESET_TEXT_COLOR);
        output.append(WHITE_COLUMN_LABELS);
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
        output.append(WHITE_COLUMN_LABELS);
        return output.toString();
    }

    private String drawBlackPOV() {
        var output = new StringBuilder();
        output.append(BLACK_COLUMN_LABELS);
        output.append("\n");
        //Top is 1, bottom is 8
        for (int i = 1; i <= 8; i++) {
            if (isEven(i)) {
                //Light first
                lightPopulatedRow(output, i);
            }
            else {
                //Dark first
                darkPopulatedRow(output, i);
            }
        }
        output.append(BLACK_COLUMN_LABELS);
        return output.toString();
    }

    private String drawWhitePOVHighlighted(ChessPosition startPosition) {
        var output = new StringBuilder();
        output.append(RESET_TEXT_COLOR);
        output.append(WHITE_COLUMN_LABELS);
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
        output.append(WHITE_COLUMN_LABELS);
        return output.toString();
    }

    private String drawBlackPOVHighlighted(ChessPosition startPosition) {
        return null;
    }

    private String pieceOnLight(ChessPiece piece) {
        if (piece == null) {
            return LIGHT_FILLER;
        }
        String pieceIcon = getPieceIcon(piece);
        return LIGHT_BG + pieceIcon + RESET_BG_COLOR;

    }

    private String pieceOnDark(ChessPiece piece) {
        if (piece == null) {
            return DARK_FILLER;
        }
        String pieceIcon = getPieceIcon(piece);
        return DARK_BG + pieceIcon + RESET_BG_COLOR;
    }

    private String pieceOnLightHighlight(ChessPiece piece) {
        if (piece == null) {
            return LIGHT_HIGHLIGHT_FILLER;
        }
        String pieceIcon = getPieceIcon(piece);
        return LIGHT_HIGHLIGHT_BG + pieceIcon + RESET_BG_COLOR;

    }

    private String pieceOnDarkHighlight(ChessPiece piece) {
        if (piece == null) {
            return DARK_HIGHLIGHT_FILLER;
        }
        String pieceIcon = getPieceIcon(piece);
        return DARK_HIGHLIGHT_BG + pieceIcon + RESET_BG_COLOR;
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
        int j;
        int jIterator;
        if (perspective == ChessGame.TeamColor.WHITE) {
            j = 1;
            jIterator = 1;
        }
        else {
            j = 8;
            jIterator = -1;
        }
        while ( j >= 1 && j <= 8) {
            //Even columns dark, odd columns light
            ChessPosition position = new ChessPosition(row, j);
            if (isEven(j)) {
                output.append(pieceOnDark(board.getPiece(position)));
            }
            else {
                output.append(pieceOnLight(board.getPiece(position)));
            }
            j += jIterator;
        }
        addRowEndLabel(output, row);
        output.append("\n");
    }

    private void lightPopulatedRowHighlighted(StringBuilder output, int row, HashSet<ChessPosition> toHighlight) {
        addRowStartLabel(output, row);
        int j;
        int jIterator;
        if (perspective == ChessGame.TeamColor.WHITE) {
            j = 1;
            jIterator = 1;
        }
        else {
            j = 8;
            jIterator = -1;
        }
        while ( j >= 1 && j <= 8) {
            //Even columns dark, odd columns light
            ChessPosition position = new ChessPosition(row, j);
            if (isEven(j)) {
                if (toHighlight.contains(position)) {
                    output.append(pieceOnDarkHighlight(board.getPiece(position)));
                }
                else {
                    output.append(pieceOnDark(board.getPiece(position)));
                }
            }
            else {
                if (toHighlight.contains(position)) {
                    output.append(pieceOnLightHighlight(board.getPiece(position)));
                }
                else {
                    output.append(pieceOnLight(board.getPiece(position)));
                }

            }
            j += jIterator;
        }
        addRowEndLabel(output, row);
        output.append("\n");
    }

    private void darkPopulatedRow(StringBuilder output, int row) {
        addRowStartLabel(output, row);
        int j;
        int jIterator;
        if (perspective == ChessGame.TeamColor.WHITE) {
            j = 1;
            jIterator = 1;
        }
        else {
            j = 8;
            jIterator = -1;
        }
        while ( j >= 1 && j <= 8) {
            //Even columns light, odd columns dark
            ChessPosition position = new ChessPosition(row, j);
            if (isEven(j)) {
                output.append(pieceOnLight(board.getPiece(position)));
            }
            else {
                output.append(pieceOnDark(board.getPiece(position)));
            }
            j += jIterator;
        }
        addRowEndLabel(output, row);
        output.append("\n");
    }

    private void darkPopulatedRowHighlighted(StringBuilder output, int row, HashSet<ChessPosition> toHighlight) {
        addRowStartLabel(output, row);
        int j;
        int jIterator;
        if (perspective == ChessGame.TeamColor.WHITE) {
            j = 1;
            jIterator = 1;
        }
        else {
            j = 8;
            jIterator = -1;
        }
        while ( j >= 1 && j <= 8) {
            //Even columns light, odd columns dark
            ChessPosition position = new ChessPosition(row, j);
            if (isEven(j)) {
                if (toHighlight.contains(position)) {
                    output.append(pieceOnLightHighlight(board.getPiece(position)));
                }
                else {
                    output.append(pieceOnLight(board.getPiece(position)));
                }
            }
            else {
                if (toHighlight.contains(position)) {
                    output.append(pieceOnDarkHighlight(board.getPiece(position)));
                }
                else {
                    output.append(pieceOnDark(board.getPiece(position)));
                }
            }
            j += jIterator;
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
