package dungeon.engine;

public class Cell {
    private Thing thing; // The thing currently occupying this cell. Can be null if empty.

    /**
     * Constructs a new Cell with a specified thing.
     *
     * @param thing The thing to place in this cell. Can be null for an empty cell.
     */
    public Cell(Thing thing) {
        this.thing = thing;
    }

    /**
     * Returns the Thing currently occupying this cell.
     *
     * @return The Thing object, or null if the cell is empty.
     */
    public Thing getThing() {
        return thing;
    }

    /**
     * Sets the thing for this cell. This can be used to place a new thing,
     * or to remove a thing by setting it to null (e.g., after gold is picked up).
     *
     * @param thing The new thing to place in this cell, or null to make it empty.
     */
    public void setThing(Thing thing) {
        this.thing = thing;
    }

    /**
     * Checks if this cell is currently empty (i.e., contains no thing).
     *
     * @return true if the cell is empty, false otherwise.
     */
    public boolean isEmpty() {
        return thing == null;
    }

    /**
     * Returns the symbol of the thing in this cell for display purposes.
     * If the cell is empty, it returns a space character.
     *
     * @return The symbol of the thing, or " " if the cell is empty.
     */
    public String getDisplaySymbol() {
        return thing != null ? thing.getSymbol() : " "; // Use a space for empty cells
    }

    /**
     * Checks if the player can move onto this cell.
     * A cell is traversable if it's empty or contains a thing that is not a Wall.
     *
     * @return true if the cell is traversable, false otherwise.
     */
    public boolean isTraversable() {
        // A cell is traversable if it's empty or the thing is not a WALL.
        // If the thing is null, it's empty and thus traversable.
        // If the thing exists, check its type.
        return thing == null || thing.getType() != ThingType.WALL;
    }

    /**
     * Checks if this cell contains a specific type of thing.
     *
     * @param type The ThingType to check for.
     * @return true if the cell contains a thing of the specified type, false otherwise.
     */
    public boolean containsThingType(ThingType type) {
        return thing != null && thing.getType() == type;
    }

    /**
     * Provides a string representation of the cell, primarily for debugging.
     *
     * @return A string representing the cell's content.
     */
    @Override
    public String toString() {
        return "[" + getDisplaySymbol() + "]";
    }
}