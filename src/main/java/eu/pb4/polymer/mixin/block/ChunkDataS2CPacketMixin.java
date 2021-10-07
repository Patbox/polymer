package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ChunkDataS2CPacket.class)
public class ChunkDataS2CPacketMixin implements ChunkDataS2CPacketInterface {
    @Unique
    private WorldChunk worldChunk;

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;Z)V", at = @At("TAIL"))
    private void polymer_storeWorldChunk(WorldChunk worldChunk, LightingProvider lightingProvider, BitSet bitSet, BitSet bitSet2, boolean bl, CallbackInfo ci) {
        this.worldChunk = worldChunk;
    }

    public WorldChunk polymer_getWorldChunk() {
        return this.worldChunk;
    }
}
