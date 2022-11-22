package eu.pb4.polymer.mixin.client.block;

import eu.pb4.polymer.api.client.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements ClientBlockStorageInterface {

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    @Override
    public void polymer$setClientBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        var id = this.getSectionIndex(y);

        if (id >= 0 && id < this.sectionArray.length) {
            var section = this.getSection(id);

            if (section != null && !section.isEmpty()) {
                ((ClientBlockStorageInterface) section).polymer$setClientBlock(x, y, z, block);
            }
        }
    }

    @Override
    public ClientPolymerBlock.State polymer$getClientBlock(int x, int y, int z) {
        var id = this.getSectionIndex(y);
        if (id >= 0 && id < this.sectionArray.length) {
            var section = this.getSection(id);

            if (section != null && !section.isEmpty()) {
                return ((ClientBlockStorageInterface) section).polymer$getClientBlock(x, y, z);
            }
        }

        return ClientPolymerBlock.NONE_STATE;
    }
}
