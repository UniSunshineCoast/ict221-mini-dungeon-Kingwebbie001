package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents a Bomb in the MiniDungeon game.
 * Detonating a bomb destroys adjacent walls and traps, and increases the player's score.
 */
public class Bomb extends AbstractThing {
    public Bomb() {
        super(ThingType.BOMB, ThingType.BOMB.getSymbol());
    }
}