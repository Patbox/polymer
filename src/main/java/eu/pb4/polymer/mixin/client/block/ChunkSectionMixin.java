package eu.pb4.polymer.mixin.client.block;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.world.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements ClientBlockStorageInterface {
    private PalettedContainer<ClientPolymerBlock.State> polymer_container = new PalettedContainer<>(InternalClientRegistry.BLOCK_STATE_PALETTE, InternalClientRegistry.BLOCK_STATES, null, null, null);

    @Override
    public void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        this.polymer_container.set(x & 15, y & 15, z & 15, block);
    }

    @Override
    public ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z) {
        return this.polymer_container.get(x & 15, y & 15, z & 15);
    }
}
