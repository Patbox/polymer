package eu.pb4.polymer.core.mixin.client.block;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements ClientBlockStorageInterface {

    public LevelChunkMixin(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, PalettedContainerFactory palettesFactory, long inhabitedTime, @Nullable LevelChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, palettesFactory, inhabitedTime, sectionArray, blendingData);
    }

    @Override
    public void polymer$setClientBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        var id = this.getSectionIndex(y);

        if (id >= 0 && id < this.sections.length) {
            var section = this.getSection(id);

            if (section != null && !section.hasOnlyAir()) {
                ((ClientBlockStorageInterface) section).polymer$setClientBlock(x, y, z, block);
            }
        }
    }

    @Override
    public ClientPolymerBlock.State polymer$getClientBlock(int x, int y, int z) {
        var id = this.getSectionIndex(y);
        if (id >= 0 && id < this.sections.length) {
            var section = this.getSection(id);

            if (section != null && !section.hasOnlyAir()) {
                return ((ClientBlockStorageInterface) section).polymer$getClientBlock(x, y, z);
            }
        }

        return ClientPolymerBlock.NONE_STATE;
    }
}
