package eu.pb4.polymer.blocks.impl;

import eu.pb4.polymer.core.impl.compat.polymc.PassthroughPoly;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import net.minecraft.block.BlockState;

public class PolymerTextureBlockPoly implements BlockPoly, PassthroughPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return input;
    }
}
