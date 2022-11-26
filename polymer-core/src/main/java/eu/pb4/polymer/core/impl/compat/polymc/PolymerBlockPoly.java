package eu.pb4.polymer.core.impl.compat.polymc;


import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("deprecation")
@ApiStatus.Internal
public class PolymerBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        var player = PolymerUtils.getPlayer();
        var state = PolymerBlockUtils.getPolymerBlockState(input, player);

        if (state != input) {
            var polyMap = Util.tryGetPolyMap(player);
            return polyMap.getClientState(state, player);
        } else {
            return state;
        }
    }
}
