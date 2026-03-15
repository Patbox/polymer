package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {

    @Unique
    private final Object2LongMap<SectionPos> polymer$scheduledLightUpdates = new Object2LongArrayMap<>();
    @Shadow
    @Final
    public ChunkMap chunkMap;
    @Shadow
    @Final
    private ServerLevel level;
    @Shadow
    @Final
    private ThreadedLevelLightEngine lightEngine;

    @Shadow
    @Nullable
    public abstract LevelChunk getChunkNow(int chunkX, int chunkZ);

    @Inject(method = "tickChunks()V", at = @At("TAIL"))
    private void polymer$sendChunkUpdates(CallbackInfo ci) {
        if (this.polymer$scheduledLightUpdates.isEmpty()) {
            return;
        }

        var currentTime = this.level.getServer().getTickCount();

        this.polymer$scheduledLightUpdates.object2LongEntrySet().removeIf(entry -> {
            var sectionPos = entry.getKey();
            var sendAfterTime = entry.getLongValue();
            if (currentTime <= sendAfterTime) {
                return false;
            }

            var chunk = this.getChunkNow(sectionPos.getX(), sectionPos.getZ());
            if (chunk == null) {
                return true;
            }

            // This might not be the section that had a changing light source, but by now all sections that are affected
            // should have been scheduled to send to clients - so if marked, it's safe to clear
            var sections = chunk.getSections();
            int sectionIndex = chunk.getSectionIndexFromSectionY(sectionPos.y());
            // As there is an additional light section above and below the world, there might not even be a block section here
            if (sectionIndex >= 0 && sectionIndex < sections.length) {
                if (sections[sectionIndex] instanceof PolymerBlockPosStorage section) {
                    section.polymer$setRequireLights(false);
                }
            }

            polymer$broadcastBlockLightForSection(sectionPos);

            return true;
        });
    }

    @Unique
    private List<ServerPlayer> getPlayersWatchingChunk(ChunkPos chunkPos) {
        if (CompatStatus.IMMERSIVE_PORTALS) {
            return ImmersivePortalsUtils.getPlayerTracking(this.level.dimension(), chunkPos);
        } else {
            return this.chunkMap.getPlayers(chunkPos, false);
        }
    }

    @Unique
    private void polymer$broadcastBlockLightForSection(SectionPos pos) {
        List<ServerPlayer> players = getPlayersWatchingChunk(pos.chunk());
        if (players.isEmpty()) {
            return;
        }
        BitSet dirtyBlockLightSections = new BitSet();
        dirtyBlockLightSections.set(pos.y() - this.lightEngine.getMinLightSection());
        Packet<?> packet = new ClientboundLightUpdatePacket(pos.chunk(), this.lightEngine, new BitSet(), dirtyBlockLightSections);
        for (ServerPlayer player : players) {
            player.connection.send(packet);
        }
    }

    @Inject(method = "onLightUpdate", at = @At("TAIL"))
    private void polymer$scheduleChunkUpdates(LightLayer type, SectionPos pos, CallbackInfo ci) {
        if (type == LightLayer.BLOCK) {
            this.level.getServer().execute(() -> {
                if (polymer$hasPendingLightUpdateAround(pos) || PolymerBlockUtils.SEND_LIGHT_UPDATE_PACKET.invoker().test(this.level, pos)) {
                    var sendAfterTime = this.level.getServer().getTickCount() + PolymerImpl.LIGHT_UPDATE_TICK_DELAY;
                    this.polymer$scheduledLightUpdates.put(pos, sendAfterTime);
                }
            });
        }
    }

    @Unique
    private boolean polymer$hasPendingLightUpdateAround(SectionPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                var chunk = this.getChunkNow(pos.getX() + x, pos.getZ() + z);
                if (chunk != null) {
                    var sections = chunk.getSections();
                    var max = Math.min(chunk.getSectionIndexFromSectionY(pos.y() + 1), sections.length - 1);

                    for (var i = Math.max(0, chunk.getSectionIndexFromSectionY(pos.y() - 1)); i <= max; i++) {
                        var section = sections[i];
                        if (section != null && !section.hasOnlyAir() && ((PolymerBlockPosStorage) section).polymer$requireLights()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
