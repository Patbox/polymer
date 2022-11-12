package eu.pb4.polymer.mixin.client.block;

import eu.pb4.polymer.api.client.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import net.minecraft.world.chunk.EmptyChunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EmptyChunk.class)
public class EmptyChunkMixin implements ClientBlockStorageInterface {
    @Override
    public void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block) {

    }

    @Override
    public ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z) {
        return ClientPolymerBlock.NONE_STATE;
    }

    @Override
    public boolean polymer_hasClientPalette() {
        return false;
    }
}
