package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.impl.interfaces.ChunkDataS2CPacketInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public class ClientboundLevelChunkWithLightPacketMixin implements ChunkDataS2CPacketInterface {
    @Unique
    private LevelChunk polymer$worldChunk;

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V", at = @At("TAIL"))
    private void polymer$storeWorldChunk(LevelChunk chunk, LevelLightEngine lightingProvider, BitSet bitSet, BitSet bitSet2, CallbackInfo ci) {
        this.polymer$worldChunk = chunk;
    }

    public LevelChunk polymer$getWorldChunk() {
        return this.polymer$worldChunk;
    }
}
