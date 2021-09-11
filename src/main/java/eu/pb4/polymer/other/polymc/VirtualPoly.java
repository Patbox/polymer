package eu.pb4.polymer.other.polymc;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class VirtualPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return BlockHelper.getBlockStateSafely((VirtualBlock) input.getBlock(), input);
    }

    @Override
    public void addToResourcePack(Block block, ResourcePackMaker pack) {

    }
}
