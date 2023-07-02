package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtInt;
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
            if (player == null) {
                return instance.getData();
            }
            int bits;

            var playerBitCount = PolymerServerNetworking.getMetadata(player.networkHandler, ClientMetadataKeys.BLOCKSTATE_BITS, NbtInt.TYPE);
            if (playerBitCount == null) {
                bits = ((PolymerIdList<?>) Block.STATE_IDS).polymer$getVanillaBitCount();
            } else {
                bits = playerBitCount.intValue();
            }

            final int size = instance.getSize();
            var data = new PackedIntegerArray(bits, size);

            for (int i = 0; i < size; i++) {
                data.set(i, palette.index(PolymerBlockUtils.getPolymerBlockState(palette.get(instance.get(i)), player)));
            }

            return data.getData();
        }

        return instance.getData();
    }
}
