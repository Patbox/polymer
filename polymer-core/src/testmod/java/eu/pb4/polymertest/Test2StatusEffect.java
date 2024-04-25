package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.BlockPositionSource;

public class Test2StatusEffect extends StatusEffect implements PolymerStatusEffect {
    protected Test2StatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x000000, new VibrationParticleEffect(new BlockPositionSource(BlockPos.ORIGIN), 1000));
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getMainHandStack().isDamageable()) {
            entity.getMainHandStack().damage(amplifier + 1, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
