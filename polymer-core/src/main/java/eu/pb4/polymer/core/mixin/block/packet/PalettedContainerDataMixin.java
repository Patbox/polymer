package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.nbt.IntTag;
import net.minecraft.util.BitStorage;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PalettedContainer.Data.class)
public abstract class PalettedContainerDataMixin<T> {
    @Shadow public abstract Palette<T> palette();

    @Shadow public abstract BitStorage storage();

    @ModifyReturnValue(method = "getSerializedSize", at = @At("RETURN"))
    private int changeCalculatedSize(int value) {
        var palette = this.palette();
        if (palette instanceof GlobalPalette<T> && palette.valueFor(0) instanceof BlockState) {
            var player = PacketContext.get();
            if (player.getClientConnection() == null) {
                return value;
            }

            var storage = this.storage();
            value -= storage.getRaw().length * 8;
            int bits;

            var playerBitCount = PolymerServerNetworking.getMetadata(player.getClientConnection(), ClientMetadataKeys.BLOCKSTATE_BITS, IntTag.TYPE);
            if (playerBitCount == null) {
                bits = PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC
                        ? ((PolymerIdMapper<?>) Block.BLOCK_STATE_REGISTRY).polymer$getVanillaBitCount()
                        : ((PolymerIdMapper<?>) Block.BLOCK_STATE_REGISTRY).polymer$getNonPolymerBitCount();
            } else {
                bits = playerBitCount.intValue();
            }

            var elementsPerLong = (char)(64 / bits);
            value += (storage.getSize() + elementsPerLong - 1) / elementsPerLong * 8;
        }
        return value;
    }

    @ModifyReceiver(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/BitStorage;getRaw()[J"), require = 0)
    private BitStorage polymer$replaceData(BitStorage storage) {
        var palette = this.palette();
        if (palette instanceof GlobalPalette<T> && palette.valueFor(0) instanceof BlockState) {
            var player = PacketContext.get();
            if (player.getClientConnection() == null) {
                return storage;
            }
            int bits;

            var playerBitCount = PolymerServerNetworking.getMetadata(player.getClientConnection(), ClientMetadataKeys.BLOCKSTATE_BITS, IntTag.TYPE);
            if (playerBitCount == null) {
                bits = PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC
                        ? ((PolymerIdMapper<?>) Block.BLOCK_STATE_REGISTRY).polymer$getVanillaBitCount()
                        : ((PolymerIdMapper<?>) Block.BLOCK_STATE_REGISTRY).polymer$getNonPolymerBitCount();
            } else {
                bits = playerBitCount.intValue();
            }
            final int size = storage.getSize();
            var data = new SimpleBitStorage(bits, size);

            var stateMap = Block.BLOCK_STATE_REGISTRY;

            for (int i = 0; i < size; i++) {
                data.set(i, stateMap.getId(PolymerBlockUtils.getPolymerBlockState(stateMap.byId(storage.get(i)), player)));
            }

            return data;
        }

        return storage;
    }
}
