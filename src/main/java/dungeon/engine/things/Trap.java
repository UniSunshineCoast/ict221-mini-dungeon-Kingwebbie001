package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents a Trap in the MiniDungeon game.
 * Interacting with a trap decreases the player's HP.
 */
public class Trap extends AbstractThing {
    public Trap() {
        super(ThingType.TRAP, ThingType.TRAP.getSymbol());
    }
}
