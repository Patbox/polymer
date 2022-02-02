package eu.pb4.polymer.interfaces;

import eu.pb4.polymer.api.other.PolymerStatusEffect;
import net.minecraft.entity.effect.StatusEffect;

/**
 * Use {@link eu.pb4.polymer.api.other.PolymerStatusEffect} instead
 */
@Deprecated
public interface VirtualStatusEffect extends PolymerStatusEffect, VirtualObject {

    default StatusEffect getVirtualStatusEffect() {
        return this.getPolymerStatusEffect();
    }

    @Override
    default StatusEffect getPolymerStatusEffect() {
        return this.getVirtualStatusEffect();
    }
}
