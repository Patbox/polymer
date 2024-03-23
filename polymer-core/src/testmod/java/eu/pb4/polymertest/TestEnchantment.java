package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Rarity;

import java.util.Optional;


public class TestEnchantment extends Enchantment implements PolymerSyncedObject<Enchantment> {
    public TestEnchantment() {
        super(new Properties(ItemTags.SWORD_ENCHANTABLE, Optional.empty(), 5, 10, new Cost(0, 30), new Cost(0, 30), 2, new EquipmentSlot[] {EquipmentSlot.MAINHAND}));
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level) {
        if(target instanceof LivingEntity) {
            ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 2 * level, level - 1));
        }

        super.onTargetDamaged(user, target, level);
    }

    @Override
    public Enchantment getPolymerReplacement(ServerPlayerEntity player) {
        return null;
    }
}
