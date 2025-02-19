package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.impl.interfaces.ChunkDataS2CPacketInterface;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ChunkDataS2CPacket.class)
public class ChunkDataS2CPacketMixin implements ChunkDataS2CPacketInterface {
    @Unique
    private WorldChunk polymer$worldChunk;

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;)V", at = @At("TAIL"))
    private void polymer$storeWorldChunk(WorldChunk chunk, LightingProvider lightingProvider, BitSet bitSet, BitSet bitSet2, CallbackInfo ci) {
        this.polymer$worldChunk = chunk;
    }

    public WorldChunk polymer$getWorldChunk() {
        return this.polymer$worldChunk;
    }
}
