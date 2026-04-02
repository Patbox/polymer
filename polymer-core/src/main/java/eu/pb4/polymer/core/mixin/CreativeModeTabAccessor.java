package eu.pb4.polymer.core.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.item.CreativeModeTab.class)
public interface CreativeModeTabAccessor {
    @Accessor
    void setIconItemStack(ItemStack iconItemStack);
}
