package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.other.PolymerMobEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.BlockPositionSource;

public class Test2StatusEffect extends MobEffect implements PolymerMobEffect {
    protected Test2StatusEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x000000, new VibrationParticleOption(new BlockPositionSource(BlockPos.ZERO), 1000));
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        if (entity.getMainHandItem().isDamageableItem()) {
            entity.getMainHandItem().hurtAndBreak(amplifier + 1, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }



    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
