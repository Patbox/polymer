package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;
import java.util.List;

public final class PolymerLightUpdateHelper {
    public static final ScopedValue<LevelChunk> CHUNK_CONTEXT = ScopedValue.newInstance();
    private static final Direction[] LIGHT_SAMPLE_DIRECTIONS = new Direction[] {
            Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private PolymerLightUpdateHelper() {
    }

    public static void patchLightData(ClientboundLightUpdatePacketData data, ChunkPos chunkPos, LevelLightEngine lightEngine,
                                      @Nullable BitSet skyChangedLightSectionFilter, @Nullable BitSet blockChangedLightSectionFilter) {
        if (!CHUNK_CONTEXT.isBound()) return;
        var chunk = CHUNK_CONTEXT.get();

        if (chunk == null || !chunk.getPos().equals(chunkPos) || !((PolymerBlockPosStorage) chunk).polymer$hasAny()) {
            return;
        }

        patchLightLayer(data.getSkyYMask(), data.getSkyUpdates(), chunk, lightEngine, LightLayer.SKY, skyChangedLightSectionFilter);
        patchLightLayer(data.getBlockYMask(), data.getBlockUpdates(), chunk, lightEngine, LightLayer.BLOCK, blockChangedLightSectionFilter);
    }

    private static void patchLightLayer(BitSet mask, List<byte[]> updates, LevelChunk chunk, LevelLightEngine lightEngine,
                                        LightLayer layer, @Nullable BitSet changedLightSectionFilter) {
        var listener = lightEngine.getLayerListener(layer);
        var sections = chunk.getSections();
        var mutable = new BlockPos.MutableBlockPos();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section == null) {
                continue;
            }

            var storage = (PolymerBlockPosStorage) section;
            if (!storage.polymer$hasAny()) {
                continue;
            }

            int sectionY = chunk.getSectionYFromSectionIndex(sectionIndex);
            int lightSectionIndex = sectionY - lightEngine.getMinLightSection();

            if (changedLightSectionFilter != null && !changedLightSectionFilter.get(lightSectionIndex)) {
                continue;
            }

            if (!mask.get(lightSectionIndex)) {
                continue;
            }
            byte[] update = updates.get(mask.get(0, lightSectionIndex).cardinality());

            for (var iterator = storage.polymer$iterator(SectionPos.of(chunk.getPos(), sectionY)); iterator.hasNext();) {
                var pos = iterator.next();
                int value = getBestLightValue(listener, mutable, pos);

                setNibble(update, pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, value);
            }
        }
    }

    private static int getBestLightValue(LayerLightEventListener listener, BlockPos.MutableBlockPos mutable, BlockPos pos) {
        int value = listener.getLightValue(pos);

        for (var direction : LIGHT_SAMPLE_DIRECTIONS) {
            mutable.setWithOffset(pos, direction);
            value = Math.max(value, listener.getLightValue(mutable));
        }

        return value;
    }

    private static void setNibble(byte[] data, int x, int y, int z, int val) {
        // Matches DataLayer.set(int x, int y, int z, int val)
        int index = y << 8 | z << 4 | x;
        int position = index >> 1;
        int nibble = index & 1;
        int mask = ~(15 << 4 * nibble);
        int valueToSet = (val & 0xF) << 4 * nibble;
        data[position] = (byte)(data[position] & mask | valueToSet);
    }
}
