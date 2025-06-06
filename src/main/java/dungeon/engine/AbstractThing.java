package dungeon.engine;

/**
 * An abstract base class that provides common implementations for the Thing interface.
 * All concrete game entities (like Gold, Trap, Wall, etc.) can extend this class
 * to inherit the basic properties and methods defined here, reducing code duplication.
 */
public abstract class AbstractThing implements Thing {
    protected ThingType type;
    protected String symbol;

    /**
     * Constructs an AbstractThing with a specified type and symbol.
     *
     * @param type   The ThingType of this entity.
     * @param symbol The character symbol representing this entity on the map.
     */
    public AbstractThing(ThingType type, String symbol) {
        this.type = type;
        this.symbol = symbol;
    }

    /**
     * Returns the character symbol associated with this thing.
     *
     * @return The symbol of the thing.
     */
    @Override
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the type of this thing.
     *
     * @return The ThingType of this thing.
     */
    @Override
    public ThingType getType() {
        return type;
    }
}