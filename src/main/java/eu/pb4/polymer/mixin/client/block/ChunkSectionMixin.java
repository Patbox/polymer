package eu.pb4.polymer.mixin.client.block;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements ClientBlockStorageInterface {
    private PalettedContainer<ClientPolymerBlock.State> polymer_container = new PalettedContainer<>(InternalClientRegistry.BLOCK_STATES, ClientPolymerBlock.NONE_STATE, PalettedContainer.PaletteProvider.BLOCK_STATE);

    @Override
    public void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        this.polymer_container.set(x & 15, y & 15, z & 15, block);
    }

    @Override
    public ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z) {
        return this.polymer_container.get(x & 15, y & 15, z & 15);
    }
}
