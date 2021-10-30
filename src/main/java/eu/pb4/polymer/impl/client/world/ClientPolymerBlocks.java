package eu.pb4.polymer.impl.client.world;

import eu.pb4.polymer.api.client.block.ClientPolymerBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import org.jetbrains.annotations.Nullable;

public class ClientPolymerBlocks {
    public static final IdList<ClientPolymerBlock> BLOCKS = new IdList<>();
    public static final IdList<ClientPolymerBlock.State> BLOCKSTATES = new IdList<>();
    public static final Palette<ClientPolymerBlock.State> PALETTE = new IdListPalette<>(BLOCKSTATES, null);

    @Nullable
    public static ClientPolymerBlock.State getBlockAt(BlockPos pos) {
        var chunk = MinecraftClient.getInstance().world.getChunk(pos);

        if (chunk instanceof ClientBlockStorageInterface storage) {
            return storage.polymer_getClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ());
        }

        return null;
    }
}
