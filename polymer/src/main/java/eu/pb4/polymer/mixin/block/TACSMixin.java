package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.impl.interfaces.ServerChunkManagerInterface;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public class TACSMixin {
    @Shadow @Final private ServerWorld world;


    @Inject(method = "sendChunkDataPackets", at = @At("HEAD"), require = 0)
    private void polymer_catchPlayer(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk, CallbackInfo ci) {
        PolymerImplUtils.setPlayer(player);
        var value = cachedDataPacket.getValue();
        var playerMapper = BlockMapper.getFrom(player);
        if (value != null && (
                ((ChunkDataS2CPacketInterface) value).polymer_hasPlayerDependentBlocks()
                || ((ChunkDataS2CPacketInterface) value).polymer_getMapper() != playerMapper
                || playerMapper != BlockMapper.createDefault()
        )) {
            cachedDataPacket.setValue(null);
        }
    }

    @Inject(method = "sendChunkDataPackets", at = @At("TAIL"), require = 0)
    private void polymer_clearPlayer(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk, CallbackInfo ci) {
        PolymerImplUtils.setPlayer(null);
    }

    @Inject(method = "method_18843", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setLoadedToWorld(Z)V", shift = At.Shift.AFTER))
    private void polymer_unloadChunk(ChunkHolder chunkHolder, CompletableFuture completableFuture, long l, Chunk chunk, CallbackInfo ci) {
        for (int i = chunk.getBottomSectionCoord(); i <= chunk.getTopSectionCoord(); i++) {
            ((ServerChunkManagerInterface) this.world.getChunkManager()).polymer_removeSection(ChunkSectionPos.from(chunk.getPos(), i));
        }
    }

    @Inject(method = "method_17227", at = @At("TAIL"))
    private void polymer_loadChunk(ChunkHolder chunkHolder, Chunk chunk, CallbackInfoReturnable<Chunk> callbackInfoReturnable) {
        for (var section : chunk.getSectionArray()) {
            if (section != null && !section.isEmpty()) {
                ((ServerChunkManagerInterface) this.world.getChunkManager()).polymer_setSection(
                        ChunkSectionPos.from(chunk.getPos(), ChunkSectionPos.getSectionCoord(section.getYOffset())),
                        section.hasAny(PolymerBlockUtils.IS_POLYMER_BLOCK_STATE_PREDICATE)
                );
            }
        }
    }
}
