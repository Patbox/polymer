package eu.pb4.polymer.core.mixin;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NbtComponent.class)
public interface NbtComponentAccessor {
    @Accessor("nbt")
    NbtCompound polymer$getNbtUnsafe();
}
