package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class TestStatusEffect extends MobEffect implements PolymerStatusEffect {
    protected TestStatusEffect() {
        super(MobEffectCategory.BENEFICIAL, 110011);
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

    @Override
    public MobEffect getPolymerReplacement(MobEffect effect, PacketContext context) {
        return MobEffects.CONDUIT_POWER.value();
    }
}
