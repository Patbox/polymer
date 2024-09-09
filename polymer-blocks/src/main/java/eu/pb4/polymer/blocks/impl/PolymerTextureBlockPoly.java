package eu.pb4.polymer.blocks.impl;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import net.minecraft.block.BlockState;

public class PolymerTextureBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return input;
    }
}
