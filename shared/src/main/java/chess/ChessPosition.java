package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    public ChessPosition(int row, int col) {
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        throw new RuntimeException("Not implemented");
    }

    private final int thisRow;
    private final int thisCol;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return thisRow == that.thisRow && thisCol == that.thisCol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(thisRow, thisCol);
    }

    @Override
    public String toString() {
        return "ChessPosition (" +
                 + thisRow +
                "," + thisCol +
                ')';
    }
}
