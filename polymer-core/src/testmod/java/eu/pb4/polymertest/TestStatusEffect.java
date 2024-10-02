package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class TestStatusEffect extends StatusEffect implements PolymerStatusEffect {
    protected TestStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 110011);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity.getMainHandStack().isDamageable()) {
            entity.getMainHandStack().damage(amplifier + 1, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public StatusEffect getPolymerReplacement(ServerPlayerEntity player) {
        return StatusEffects.CONDUIT_POWER.value();
    }
}
