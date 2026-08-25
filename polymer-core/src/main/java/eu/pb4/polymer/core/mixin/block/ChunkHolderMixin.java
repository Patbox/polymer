package eu.pb4.polymer.core.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.core.impl.PolymerLightUpdateHelper;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.BitSet;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {
    @WrapOperation(
        method = "broadcastChanges",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)Lnet/minecraft/network/protocol/game/ClientboundLightUpdatePacket;"
        )
    )
    private ClientboundLightUpdatePacket addPolymerLightContext(
        ChunkPos pos, LevelLightEngine lightEngine, @Nullable BitSet skyChangedLightSectionFilter,
        @Nullable BitSet blockChangedLightSectionFilter, Operation<ClientboundLightUpdatePacket> operation,
        LevelChunk chunk
    ) {
        return ScopedValue.where(PolymerLightUpdateHelper.CHUNK_CONTEXT, chunk)
            .call(() -> operation.call(pos, lightEngine, skyChangedLightSectionFilter, blockChangedLightSectionFilter));
    }
}
