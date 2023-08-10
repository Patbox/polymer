package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.impl.interfaces.ItemStackAwareNbtCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(NbtCompound.class)
public class NbtCompoundFallbackMixin implements ItemStackAwareNbtCompound {

}
