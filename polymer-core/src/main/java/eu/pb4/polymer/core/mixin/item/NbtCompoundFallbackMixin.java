package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.impl.interfaces.TypeAwareNbtCompound;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NbtCompound.class)
public class NbtCompoundFallbackMixin implements TypeAwareNbtCompound {

}
