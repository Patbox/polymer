package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.core.impl.PolymerLightUpdateHelper;
import eu.pb4.polymer.core.impl.interfaces.ChunkDataS2CPacketInterface;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public class ClientboundLevelChunkWithLightPacketMixin implements ChunkDataS2CPacketInterface {
    @Unique
    private LevelChunk polymer$worldChunk;

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V", at = @At("TAIL"))
    private void polymer$storeWorldChunk(LevelChunk chunk, LevelLightEngine lightingProvider, BitSet bitSet, BitSet bitSet2, CallbackInfo ci) {
        this.polymer$worldChunk = chunk;
    }

    @WrapOperation(
        method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)Lnet/minecraft/network/protocol/game/ClientboundLightUpdatePacketData;"
        )
    )
    private ClientboundLightUpdatePacketData polymer$addPolymerLightContext(
        ChunkPos chunkPos, LevelLightEngine lightEngine, @Nullable BitSet skyChangedLightSectionFilter,
        @Nullable BitSet blockChangedLightSectionFilter, Operation<ClientboundLightUpdatePacketData> operation,
        LevelChunk chunk) {
        return ScopedValue.where(PolymerLightUpdateHelper.CHUNK_CONTEXT, chunk).call(() -> operation.call(chunkPos, lightEngine, skyChangedLightSectionFilter, blockChangedLightSectionFilter));
    }

    public LevelChunk polymer$getWorldChunk() {
        return this.polymer$worldChunk;
    }
}
