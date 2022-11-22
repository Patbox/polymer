package eu.pb4.polymer.mixin.block.storage;

import eu.pb4.polymer.impl.interfaces.PolymerBlockPosStorage;
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
    private final ShortSet polymer_blocks = new ShortOpenHashSet();

    @Override
    public @Nullable ShortSet polymer$getBackendSet() {
        return this.polymer_blocks;
    }

    @Override
    public Iterator<BlockPos.Mutable> polymer$iterator(ChunkSectionPos sectionPos) {
        var blockPos = new BlockPos.Mutable();
        var iterator = this.polymer_blocks.iterator();

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
    public void polymer$setPolymer(int x, int y, int z) {
        this.polymer_blocks.add(PolymerBlockPosStorage.pack(x, y, z));
    }

    @Override
    public void polymer$removePolymer(int x, int y, int z) {
        this.polymer_blocks.remove(PolymerBlockPosStorage.pack(x, y, z));
    }

    @Override
    public boolean polymer$getPolymer(int x, int y, int z) {
        return this.polymer_blocks.contains(PolymerBlockPosStorage.pack(x, y, z));
    }

    @Override
    public boolean polymer$hasAny() {
        return !this.polymer_blocks.isEmpty();
    }
}
