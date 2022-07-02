package eu.pb4.polymer.impl.compat.polymc;


import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("deprecation")
@ApiStatus.Internal
public class PolymerBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        var polyMap = Util.tryGetPolyMap(PolymerUtils.getPlayer());
        return polyMap.getClientState(PolymerBlockUtils.getPolymerBlockState(input, PolymerUtils.getPlayer()), PolymerUtils.getPlayer());
    }
}
