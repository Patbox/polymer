package eu.pb4.polymer.core.mixin.block;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PalettedContainer.class)
public interface PalettedContainerAccessor {
    @Accessor
    PalettedContainer.Data<?> getData();

    @Accessor
    PalettedContainer.PaletteProvider getPaletteProvider();
}
