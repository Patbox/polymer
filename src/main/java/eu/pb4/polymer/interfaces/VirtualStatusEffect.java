package eu.pb4.polymer.interfaces;

import net.minecraft.entity.effect.StatusEffect;

public interface VirtualStatusEffect extends VirtualObject {

    /**
     * Returns the effect to be displayed on the client's HUD.
     * Note that particle color is determined by the server, so this will not affect them.
     *
     * @return Vanilla status effect, or <code>null</>null if nothing is to be displayed by the client
     */
    default StatusEffect getVirtualStatusEffect() {
        return null;
    }
}
