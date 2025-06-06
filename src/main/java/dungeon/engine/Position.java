package dungeon.engine;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator; // Import for JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty; // Import for JsonProperty

/**
 * A simple immutable class to represent a (row, column) coordinate on the dungeon map.
 * Provides basic getters and overrides equals and hashCode for proper comparison.
 * Now includes Jackson annotations for proper JSON deserialization of immutable objects.
 */
public class Position {
    private final int row;
    private final int col;

    /**
     * Constructs a new Position object.
     * This constructor is annotated with JsonCreator and JsonProperty to allow Jackson
     * to deserialize JSON objects into immutable Position instances.
     *
     * @param row The row coordinate.
     * @param col The column coordinate.
     */
    @JsonCreator // Tells Jackson to use this constructor for deserialization
    public Position(@JsonProperty("row") int row, @JsonProperty("col") int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the row coordinate.
     *
     * @return The row.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column coordinate.
     *
     * @return The column.
     */
    public int getCol() {
        return col;
    }

    /**
     * Compares this Position object to another object for equality.
     * Two Position objects are considered equal if their row and column coordinates are the same.
     *
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    /**
     * Returns a hash code value for this Position object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Returns a string representation of this Position object.
     *
     * @return A string in the format "(row, col)".
     */
    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
