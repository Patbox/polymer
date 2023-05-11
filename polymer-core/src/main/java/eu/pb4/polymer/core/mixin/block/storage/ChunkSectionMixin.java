package eu.pb4.polymer.core.mixin.block.storage;

import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements PolymerBlockPosStorage {
    @Unique
    private final ShortSet polymer$blocks = new ShortOpenHashSet();
    @Unique
    private final ShortSet polymer$lights = new ShortOpenHashSet();
    @Unique
    private boolean polymer$requireLightUpdate;;

    @Override
    public @Nullable ShortSet polymer$getBackendSet() {
        return this.polymer$blocks;
    }

    @Override
    public Iterator<BlockPos.Mutable> polymer$iterator(ChunkSectionPos sectionPos) {
        var blockPos = new BlockPos.Mutable();
        var iterator = this.polymer$blocks.iterator();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public BlockPos.Mutable next() {
                var value = iterator.nextShort();

                return blockPos.set(sectionPos.unpackBlockX(value), sectionPos.unpackBlockY(value), sectionPos.unpackBlockZ(value));
            }
        };
    }

    @Override
    public @Nullable Iterator<BlockPos.Mutable> polymer$iterator() {
        return null;
    }

    @Override
    public void polymer$setSynced(int x, int y, int z, boolean lightSource) {
        var i = PolymerBlockPosStorage.pack(x, y, z);
        this.polymer$blocks.add(i);
        if (lightSource) {
            this.polymer$lights.add(i);
        }
    }

    @Override
    public void polymer$removeSynced(int x, int y, int z) {
        var i = PolymerBlockPosStorage.pack(x, y, z);
        this.polymer$blocks.remove(i);
        if (this.polymer$lights.remove(i)) {
            this.polymer$requireLightUpdate = true;
        }
    }

    @Override
    public boolean polymer$isSynced(int x, int y, int z) {
        return this.polymer$blocks.contains(PolymerBlockPosStorage.pack(x, y, z));
    }

    @Override
    public boolean polymer$hasAny() {
        return !this.polymer$blocks.isEmpty();
    }

    @Override
    public boolean polymer$hasLights() {
        return !this.polymer$lights.isEmpty();
    }

    @Override
    public boolean polymer$requireLights() {
        return this.polymer$requireLightUpdate || polymer$hasLights();
    }

    @Override
    public void polymer$setRequireLights(boolean value) {
        this.polymer$requireLightUpdate = value;
    }
}
