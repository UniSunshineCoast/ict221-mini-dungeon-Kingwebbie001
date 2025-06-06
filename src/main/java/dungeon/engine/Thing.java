package dungeon.engine;


/**
 * An interface representing any "thing" that can be placed on a cell in the MiniDungeon game map.
 * This interface ensures that all game entities provide a symbol for display and can
 * potentially define their interaction behavior with the player.
 *
 * By using an interface, we can achieve polymorphism, allowing the Cell class to hold
 * any type of game thing without needing to know its specific concrete class.
 */
public interface Thing {
    /**
     * Returns the character symbol that represents this thing on the game map.
     * This symbol is used for rendering the thing in the text-based user interface.
     *
     * @return The symbol of the thing.
     */
    String getSymbol();

    /**
     * Returns the type of this thing, as defined by the ThingType enum.
     * This helps in identifying the general category of the thing.
     *
     * @return The ThingType of this thing.
     */
    ThingType getType();
}