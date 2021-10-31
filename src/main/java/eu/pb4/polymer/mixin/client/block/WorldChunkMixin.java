package eu.pb4.polymer.mixin.client.block;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.world.ClientBlockStorageInterface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ClientBlockStorageInterface, Chunk {
    @Shadow public abstract ChunkSection[] getSectionArray();

    @Override
    public void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        ((ClientBlockStorageInterface) this.getSection(this.getSectionIndex(y))).polymer_setClientPolymerBlock(x, y, z, block);
    }

    @Override
    public ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z) {
        return ((ClientBlockStorageInterface) this.getSection(this.getSectionIndex(y))).polymer_getClientPolymerBlock(x, y, z);
    }
}
