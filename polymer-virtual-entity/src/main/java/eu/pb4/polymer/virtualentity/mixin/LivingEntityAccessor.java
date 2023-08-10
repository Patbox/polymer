package eu.pb4.polymer.virtualentity.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker
    Map<EquipmentSlot, ItemStack> callGetEquipmentChanges();

    @Invoker
    void callSetSyncedHandStack(EquipmentSlot slot, ItemStack stack);

    @Invoker
    void callSetSyncedArmorStack(EquipmentSlot slot, ItemStack armor);
}
