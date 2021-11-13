package eu.pb4.polymertest;

import eu.pb4.polymer.interfaces.VirtualObject;
import eu.pb4.polymer.interfaces.VirtualStatusEffect;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;

public class TestStatusEffect extends StatusEffect implements VirtualStatusEffect {
    protected TestStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 110011);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getMainHandStack().isDamageable()) {
            entity.getMainHandStack().damage(amplifier + 1, entity, entity1 -> entity1.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public StatusEffect getVirtualStatusEffect() {
        return StatusEffects.CONDUIT_POWER;
    }
}
