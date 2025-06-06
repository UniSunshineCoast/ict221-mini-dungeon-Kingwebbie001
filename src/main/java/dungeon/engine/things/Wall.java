package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents a Wall in the MiniDungeon game.
 * Walls block player movement.
 */
public class Wall extends AbstractThing {
    public Wall() {
        super(ThingType.WALL, ThingType.WALL.getSymbol());
    }
}