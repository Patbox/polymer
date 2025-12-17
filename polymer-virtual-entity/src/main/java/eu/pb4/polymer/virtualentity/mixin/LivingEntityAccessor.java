package eu.pb4.polymer.virtualentity.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker
    Map<EquipmentSlot, ItemStack> callCollectEquipmentChanges();
}
