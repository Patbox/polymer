package eu.pb4.polymer.core.mixin.block;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PalettedContainer.class)
public interface PalettedContainerAccessor<T> {
    @Accessor
    PalettedContainer.Data<T> getData();

    @Accessor
    PalettedContainer.PaletteProvider getPaletteProvider();

    @Invoker
    void callSet(int index, T value);

    @Invoker
    T callGet(int index);
}
