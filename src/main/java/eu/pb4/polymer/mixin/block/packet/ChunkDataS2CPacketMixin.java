package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.impl.interfaces.ChunkDataS2CPacketInterface;
import net.minecraft.network.Packet;
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
    private WorldChunk polymer_worldChunk;

    @Unique
    private Packet<?>[] polymer_attachedPackets = null;

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;Z)V", at = @At("TAIL"))
    private void polymer_storeWorldChunk(WorldChunk chunk, LightingProvider lightingProvider, BitSet bitSet, BitSet bitSet2, boolean bl, CallbackInfo ci) {
        this.polymer_worldChunk = chunk;
    }

    public WorldChunk polymer_getWorldChunk() {
        return this.polymer_worldChunk;
    }
}
