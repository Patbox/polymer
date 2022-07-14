package eu.pb4.polymer.mixin.client.rendering;

import eu.pb4.polymer.impl.PolymerImpl;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {
    @ModifyVariable(method = "drawStatusEffects", at = @At("STORE"), ordinal = 0)
    private Collection<StatusEffectInstance> polymer_removeEffect(Collection<StatusEffectInstance> value) {
        if (PolymerImpl.CHANGING_QOL_CLIENT) {
            value.removeIf((x) -> x.getEffectType() == StatusEffects.MINING_FATIGUE && x.getAmplifier() == -1);
        }
        return value;
    }
}
