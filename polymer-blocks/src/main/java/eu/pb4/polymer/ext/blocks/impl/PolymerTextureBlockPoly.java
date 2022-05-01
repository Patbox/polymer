package eu.pb4.polymer.ext.blocks.impl;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class PolymerTextureBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return PolymerBlockUtils.getPolymerBlockState(input);
    }
}
