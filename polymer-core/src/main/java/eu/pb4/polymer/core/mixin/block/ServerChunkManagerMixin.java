package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ServerChunkLoadingManager;
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

import java.util.BitSet;
import java.util.List;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {

    @Unique
    private final Object2LongMap<ChunkSectionPos> polymer$scheduledLightUpdates = new Object2LongArrayMap<>();
    @Shadow
    @Final
    public ServerChunkLoadingManager chunkLoadingManager;
    @Shadow
    @Final
    private ServerWorld world;
    @Shadow
    @Final
    private ServerLightingProvider lightingProvider;

    @Shadow
    @Nullable
    public abstract WorldChunk getWorldChunk(int chunkX, int chunkZ);

    @Inject(method = "tickChunks", at = @At("TAIL"))
    private void polymer$sendChunkUpdates(CallbackInfo ci) {
        if (this.polymer$scheduledLightUpdates.isEmpty()) {
            return;
        }

        var currentTime = this.world.getServer().getTicks();

        this.polymer$scheduledLightUpdates.object2LongEntrySet().removeIf(entry -> {
            var sectionPos = entry.getKey();
            var sendAfterTime = entry.getLongValue();
            if (currentTime <= sendAfterTime) {
                return false;
            }

            var chunk = this.getWorldChunk(sectionPos.getX(), sectionPos.getZ());
            if (chunk == null) {
                return true;
            }

            // This might not be the section that had a changing light source, but by now all sections that are affected
            // should have been scheduled to send to clients - so if marked, it's safe to clear
            var sections = chunk.getSectionArray();
            int sectionIndex = chunk.sectionCoordToIndex(sectionPos.getSectionY());
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
    private void polymer$broadcastBlockLightForSection(ChunkSectionPos pos) {
        List<ServerPlayerEntity> players = this.chunkLoadingManager.getPlayersWatchingChunk(pos.toChunkPos(), false);
        if (players.isEmpty()) {
            return;
        }
        BitSet dirtyBlockLightSections = new BitSet();
        dirtyBlockLightSections.set(pos.getSectionY() - this.lightingProvider.getBottomY());
        Packet<?> packet = new LightUpdateS2CPacket(pos.toChunkPos(), this.lightingProvider, new BitSet(), dirtyBlockLightSections);
        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(packet);
        }
    }

    @Inject(method = "onLightUpdate", at = @At("TAIL"))
    private void polymer$scheduleChunkUpdates(LightType type, ChunkSectionPos pos, CallbackInfo ci) {
        if (type == LightType.BLOCK) {
            this.world.getServer().execute(() -> {
                if (polymer$hasPendingLightUpdateAround(pos) || PolymerBlockUtils.SEND_LIGHT_UPDATE_PACKET.invoke((c) -> c.test(this.world, pos))) {
                    var sendAfterTime = this.world.getServer().getTicks() + PolymerImpl.LIGHT_UPDATE_TICK_DELAY;
                    this.polymer$scheduledLightUpdates.put(pos, sendAfterTime);
                }
            });
        }
    }

    @Unique
    private boolean polymer$hasPendingLightUpdateAround(ChunkSectionPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                var chunk = this.getWorldChunk(pos.getX() + x, pos.getZ() + z);
                if (chunk != null) {
                    var sections = chunk.getSectionArray();
                    var max = Math.min(chunk.sectionCoordToIndex(pos.getSectionY() + 1), sections.length - 1);

                    for (var i = Math.max(0, chunk.sectionCoordToIndex(pos.getSectionY() - 1)); i <= max; i++) {
                        var section = sections[i];
                        if (section != null && !section.isEmpty() && ((PolymerBlockPosStorage) section).polymer$requireLights()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
