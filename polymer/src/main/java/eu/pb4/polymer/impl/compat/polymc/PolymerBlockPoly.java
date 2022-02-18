package eu.pb4.polymer.impl.compat.polymc;


import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("deprecation")
@ApiStatus.Internal
public class PolymerBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return PolyMc.getMainMap().getClientBlock(PolymerBlockUtils.getPolymerBlockState(input, PolymerUtils.getPlayer()));
    }

    @Override
    public void addToResourcePack(Block block, ResourcePackMaker pack) {

    }
}
