package eu.pb4.polymertest;

import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TestStatusEffect extends StatusEffect implements VirtualObject {
    protected TestStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 110011);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getMainHandStack().isDamageable()) {
            entity.getMainHandStack().damage(1, entity, entity1 -> entity1.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
    }
}
