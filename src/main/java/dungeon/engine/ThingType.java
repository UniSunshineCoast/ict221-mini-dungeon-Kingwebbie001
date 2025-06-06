package dungeon.engine;

/**
 * Represents the different types of "things" that can exist on the MiniDungeon game map.
 * This enum provides a structured way to define the unique symbol and a brief description
 * for each thing, making the game logic cleaner and more readable.
 *
 */
public enum ThingType {
    ENTRY("E", "Entry point into the maze"),
    LADDER("L", "Advances to the next level or exits the game"),
    PLAYER("P", "Moves in 4 directions: left, right, up, and down"),
    WALL("#", "Blocks movement"),
    TRAP("T", "Decreases player's HP"),
    GOLD("G", "Increases player's score"),
    MELEE_MUTANT("M", "Stationary; stepping on it reduces HP and increases score"),
    RANGED_MUTANT("R", "Stationary; attacks from 2 tiles away, stepping on it increases score"),
    HEALTH_POTION("H", "Restores player's HP"),
    BOMB("B", "Activatable item: Destroys adjacent walls and traps, increases score"); // UPDATED: Bomb description

    private final String symbol;
    private final String description;

    ThingType(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public static ThingType fromSymbol(String symbol) {
        for (ThingType type : ThingType.values()) {
            if (type.getSymbol().equals(symbol)) {
                return type;
            }
        }
        return null;
    }
}