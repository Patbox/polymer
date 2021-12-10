package eu.pb4.polymer.mixin.other;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PalettedContainer.class)
public interface PalettedContainerAccessor<T> {
    @Invoker("set")
    void polymer_set(int index, T value);

    @Accessor("paletteProvider")
    PalettedContainer.PaletteProvider polymer_paletteProvider();
}
