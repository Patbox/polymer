package eu.pb4.polymer.core.mixin.block.storage;

import eu.pb4.polymer.core.impl.interfaces.PolymerChunkSectionStorage;
import eu.pb4.polymer.core.impl.other.PolymerSectionBitStorage;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunkSection;

@Mixin(LevelChunkSection.class)
public class LevelChunkSectionMixin implements PolymerChunkSectionStorage {
    @Unique
    private final PolymerSectionBitStorage polymer$blocks = new PolymerSectionBitStorage();
    @Unique
    private final PolymerSectionBitStorage polymer$lights = new PolymerSectionBitStorage();
    @Unique
    private final PolymerSectionBitStorage polymer$lightInsides = new PolymerSectionBitStorage();
    @Unique
    private boolean polymer$requireLightUpdate;;

    @Override
    public ShortIterator polymer$getRawBlockIterator() {
        return this.polymer$blocks.shortIterator();
    }

    @Unique
    public Iterator<BlockPos.MutableBlockPos> polymer$iterator(ShortIterator iterator, SectionPos sectionPos) {
        var blockPos = new BlockPos.MutableBlockPos();

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
    public Iterator<BlockPos.MutableBlockPos> polymer$blockIterator(SectionPos sectionPos) {
        return polymer$iterator(this.polymer$blocks.shortIterator(), sectionPos);
    }

    @Override
    public Iterator<BlockPos.MutableBlockPos> polymer$lightInsideIterator(SectionPos sectionPos) {
        return polymer$iterator(this.polymer$lightInsides.shortIterator(), sectionPos);
    }

    @Override
    public void polymer$setSynced(int x, int y, int z, boolean lightSource, boolean lightInside) {
        var i = PolymerChunkSectionStorage.pack(x, y, z);
        this.polymer$blocks.set(i);
        if (lightSource) {
            this.polymer$lights.set(i);
        }
        if (lightInside) {
            this.polymer$lightInsides.set(i);
        }
    }

    @Override
    public void polymer$removeSynced(int x, int y, int z) {
        var i = PolymerChunkSectionStorage.pack(x, y, z);
        this.polymer$blocks.remove(i);
        if (this.polymer$lights.remove(i) || this.polymer$lightInsides.remove(i)) {
            this.polymer$requireLightUpdate = true;
        }
    }

    @Override
    public boolean polymer$isSynced(int x, int y, int z) {
        return this.polymer$blocks.get(PolymerChunkSectionStorage.pack(x, y, z));
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
        return this.polymer$requireLightUpdate || polymer$hasLights() || !this.polymer$lightInsides.isEmpty();
    }

    @Override
    public void polymer$setRequireLights(boolean value) {
        this.polymer$requireLightUpdate = value;
    }
}
