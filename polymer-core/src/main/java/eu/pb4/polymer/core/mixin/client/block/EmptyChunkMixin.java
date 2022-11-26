package eu.pb4.polymer.core.mixin.client.block;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import net.minecraft.world.chunk.EmptyChunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EmptyChunk.class)
public class EmptyChunkMixin implements ClientBlockStorageInterface {
    @Override
    public void polymer$setClientBlock(int x, int y, int z, ClientPolymerBlock.State block) {

    }

    @Override
    public ClientPolymerBlock.State polymer$getClientBlock(int x, int y, int z) {
        return ClientPolymerBlock.NONE_STATE;
    }
}
