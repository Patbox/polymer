package eu.pb4.polymer.core.mixin.block.storage;

import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunkSection;

@Mixin(LevelChunkSection.class)
public class LevelChunkSectionMixin implements PolymerBlockPosStorage {
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
    public Iterator<BlockPos.MutableBlockPos> polymer$iterator(SectionPos sectionPos) {
        var blockPos = new BlockPos.MutableBlockPos();
        var iterator = this.polymer$blocks.iterator();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public BlockPos.MutableBlockPos next() {
                var value = iterator.nextShort();

                return blockPos.set(sectionPos.relativeToBlockX(value), sectionPos.relativeToBlockY(value), sectionPos.relativeToBlockZ(value));
            }
        };
    }

    @Override
    public @Nullable Iterator<BlockPos.MutableBlockPos> polymer$iterator() {
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
