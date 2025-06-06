package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents Gold in the MiniDungeon game.
 * Collecting gold increases the player's score.
 */
public class Gold extends AbstractThing {
    public Gold() {
        super(ThingType.GOLD, ThingType.GOLD.getSymbol());
    }
}
