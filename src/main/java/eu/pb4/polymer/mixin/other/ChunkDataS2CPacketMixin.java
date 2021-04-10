package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.interfaces.ChunkDataS2CPacketInterface;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkDataS2CPacket.class)
public class ChunkDataS2CPacketMixin implements ChunkDataS2CPacketInterface {
    private WorldChunk worldChunk;

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;I)V", at = @At("TAIL"))
    private void storeWorldChunk(WorldChunk chunk, int includedSectionsMask, CallbackInfo ci) {
        this.worldChunk = chunk;
    }

    public WorldChunk getWorldChunk() {
        return this.worldChunk;
    }
}
