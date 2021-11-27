package eu.pb4.polymer.impl.compat.polymc;


import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return PolymerBlockUtils.getPolymerBlockState(input);
    }

    @Override
    public void addToResourcePack(Block block, ResourcePackMaker pack) {

    }
}
