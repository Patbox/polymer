package eu.pb4.polymer.mixin.client.block;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements ClientBlockStorageInterface {

    public WorldChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> registry, long l, @Nullable ChunkSection[] chunkSections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, heightLimitView, registry, l, chunkSections, blendingData);
    }

    @Override
    public void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        ((ClientBlockStorageInterface) this.getSection(this.getSectionIndex(y))).polymer_setClientPolymerBlock(x, y, z, block);
    }

    @Override
    public ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z) {
        return ((ClientBlockStorageInterface) this.getSection(this.getSectionIndex(y))).polymer_getClientPolymerBlock(x, y, z);
    }
}
