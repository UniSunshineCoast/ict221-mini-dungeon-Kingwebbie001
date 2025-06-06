package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents the Entry point in the MiniDungeon game.
 * Players start at this location.
 */
public class Entry extends AbstractThing {
    public Entry() {
        super(ThingType.ENTRY, ThingType.ENTRY.getSymbol());
    }
}