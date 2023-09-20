package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtInt;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PalettedContainer.Data.class)
public abstract class PalettedContainerDataMixin<T> {
    @Shadow public abstract Palette<T> palette();

    @ModifyReceiver(method = "writePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/PaletteStorage;getData()[J"), require = 0)
    private PaletteStorage polymer$replaceData(PaletteStorage storage) {
        var palette = this.palette();
        if (palette instanceof IdListPalette<T> && palette.get(0) instanceof BlockState) {
            var player = PolymerUtils.getPlayerContext();
            if (player == null) {
                return storage;
            }
            int bits;

            var playerBitCount = PolymerServerNetworking.getMetadata(player.networkHandler, ClientMetadataKeys.BLOCKSTATE_BITS, NbtInt.TYPE);
            if (playerBitCount == null) {
                bits = PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC
                        ? ((PolymerIdList<?>) Block.STATE_IDS).polymer$getVanillaBitCount()
                        : ((PolymerIdList<?>) Block.STATE_IDS).polymer$getNonPolymerBitCount();
            } else {
                bits = playerBitCount.intValue();
            }
            final int size = storage.getSize();
            var data = new PackedIntegerArray(bits, size);

            var stateMap = Block.STATE_IDS;

            for (int i = 0; i < size; i++) {
                data.set(i, stateMap.getRawId(PolymerBlockUtils.getPolymerBlockState(stateMap.get(storage.get(i)), player)));
            }

            return data;
        }

        return storage;
    }
}
