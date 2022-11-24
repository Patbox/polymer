package eu.pb4.polymertest;

import eu.pb4.polymer.api.resourcepack.PolymerResourcePackUtils;
import eu.pb4.polymer.api.utils.PolymerSyncedObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;


public class TestEnchantment extends Enchantment implements PolymerSyncedObject<Enchantment> {
    public TestEnchantment() {
        super(Rarity.COMMON, EnchantmentTarget.WEAPON, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 10;
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
        return PolymerResourcePackUtils.hasPack(player) ? this : null;
    }
}
