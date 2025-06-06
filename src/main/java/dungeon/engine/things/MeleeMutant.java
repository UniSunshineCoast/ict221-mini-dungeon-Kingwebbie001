package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents a Melee Mutant in the MiniDungeon game.
 * Stationary; stepping on it reduces HP and increases score.
 */
public class MeleeMutant extends AbstractThing {
    public MeleeMutant() {
        super(ThingType.MELEE_MUTANT, ThingType.MELEE_MUTANT.getSymbol());
    }
}
