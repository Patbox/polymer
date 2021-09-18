package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.interfaces.WorldChunkInterface;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {

    @Shadow
    @Final
    public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Shadow
    @Final
    private ServerWorld world;

    @Shadow
    public abstract ServerLightingProvider getLightingProvider();

    @Shadow
    @Nullable
    public abstract WorldChunk getWorldChunk(int chunkX, int chunkZ);

    @Shadow @Final private ServerLightingProvider lightingProvider;
    @Unique
    private final Object2LongArrayMap<ChunkSectionPos> lastUpdates = new Object2LongArrayMap<>();

    @Inject(method = "tickChunks", at = @At("TAIL"))
    private void sendChunkUpdates(CallbackInfo ci) {
        this.world.getServer().execute(() -> {
            if (this.lastUpdates.size() != 0) {
                for (var entry : new ArrayList<>(this.lastUpdates.object2LongEntrySet())) {
                    var pos = entry.getKey();
                    var time = entry.getLongValue();

                    if (System.currentTimeMillis() - time > 100) {
                        BitSet bitSet = new BitSet();
                        bitSet.set(pos.getSectionY() - this.lightingProvider.getBottomY());
                        Packet<?> packet = new LightUpdateS2CPacket(pos.toChunkPos(), this.getLightingProvider(), new BitSet(this.world.getTopSectionCoord() + 2), bitSet, true);
                        Set<ServerPlayerEntity> players = this.threadedAnvilChunkStorage.getPlayersWatchingChunk(pos.toChunkPos(), false).collect(Collectors.toSet());
                        if (players.size() > 0) {
                            this.lastUpdates.put(pos, System.currentTimeMillis());
                            for (ServerPlayerEntity player : players) {
                                player.networkHandler.sendPacket(packet);
                            }
                        }
                        this.lastUpdates.removeLong(pos);
                    }
                }
            }
        });
    }

    @Inject(method = "onLightUpdate", at = @At("TAIL"))
    private void scheduleChunkUpdates(LightType type, ChunkSectionPos pos, CallbackInfo ci) {
        if (type == LightType.BLOCK && this.world.getServer().getPlayerManager().getCurrentPlayerCount() > 0) {
            this.world.getServer().execute(() -> {
                boolean sendUpdate = false;
                int tooLow = pos.getSectionY() * 16 - 16;
                int tooHigh = pos.getSectionY() * 16 + 32;

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        WorldChunk chunk = this.getWorldChunk(pos.getX() + x, pos.getZ() + z);
                        if (chunk != null) {
                            for (BlockPos blockPos : ((WorldChunkInterface) chunk).getVirtualBlocks()) {
                                if (blockPos.getY() < tooLow || blockPos.getY() > tooHigh) {
                                    continue;
                                }

                                BlockState blockState = chunk.getBlockState(blockPos);
                                if (BlockHelper.isVirtualLightSource(blockState)) {
                                    sendUpdate = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (sendUpdate || BlockHelper.SEND_LIGHT_UPDATE_PACKET.invoke(this.world, pos)) {
                    this.lastUpdates.put(pos, System.currentTimeMillis());
                }
            });
        }
    }
}