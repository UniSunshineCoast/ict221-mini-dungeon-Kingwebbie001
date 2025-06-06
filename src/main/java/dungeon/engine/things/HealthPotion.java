package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents a Health Potion in the MiniDungeon game.
 * Consuming a health potion restores the player's HP.
 */
public class HealthPotion extends AbstractThing {
    public HealthPotion() {
        super(ThingType.HEALTH_POTION, ThingType.HEALTH_POTION.getSymbol());
    }
}
