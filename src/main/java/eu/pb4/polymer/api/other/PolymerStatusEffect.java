package eu.pb4.polymer.api.other;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.entity.effect.StatusEffect;

public interface PolymerStatusEffect extends PolymerObject {

    /**
     * Returns the effect to be displayed on the client's HUD.
     * Note that particle color is determined by the server, so this will not affect them.
     *
     * @return Vanilla status effect, or <code>null</>null if nothing is to be displayed by the client
     */
    default StatusEffect getPolymerStatusEffect() {
        return null;
    }
}
