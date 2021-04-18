package eu.pb4.polymer.polymc;

import eu.pb4.polymer.block.VirtualBlock;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class VirtualPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return ((VirtualBlock) input.getBlock()).getVirtualBlockState(input);
    }

    @Override
    public void AddToResourcePack(Block block, ResourcePackMaker pack) {}
}
