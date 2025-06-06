package dungeon.engine.things;

import dungeon.engine.AbstractThing;
import dungeon.engine.ThingType;

/**
 * Represents a Ranged Mutant in the MiniDungeon game.
 * Stationary; attacks from 2 tiles away, stepping on it increases score.
 */
public class RangedMutant extends AbstractThing {
    public RangedMutant() {
        super(ThingType.RANGED_MUTANT, ThingType.RANGED_MUTANT.getSymbol());
    }
}
