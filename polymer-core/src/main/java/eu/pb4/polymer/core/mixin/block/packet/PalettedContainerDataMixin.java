package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/world/chunk/PalettedContainer$Data")
public class PalettedContainerDataMixin<T> {
    @Shadow @Final private Palette<T> palette;

    @Redirect(method = "writePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/PaletteStorage;getData()[J"))
    private long[] polymer$replaceData(PaletteStorage instance) {
        if (this.palette instanceof IdListPalette<T>  && this.palette.get(0) instanceof BlockState) {
            var palette = (IdListPalette<BlockState>) this.palette;
            var player = PolymerUtils.getPlayerContext();
            final int size = instance.getSize();
            var data = new PackedIntegerArray(instance.getElementBits(), size);

            for (int i = 0; i < size; i++) {
                data.set(i, palette.index(PolymerBlockUtils.getPolymerBlockState(palette.get(instance.get(i)), player)));
            }

            return data.getData();
        }

        return instance.getData();
    }
}
