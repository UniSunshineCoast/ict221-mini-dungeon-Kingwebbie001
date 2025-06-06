package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents the Ladder in the MiniDungeon game.
 * Reaching this allows the player to advance to the next level or exit the game.
 */
public class Ladder extends AbstractThing {
    public Ladder() {
        super(ThingType.LADDER, ThingType.LADDER.getSymbol());
    }
}
