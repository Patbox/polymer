package eu.pb4.polymer.core.impl.compat.polymc;


import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.packettweaker.PacketContext;

@SuppressWarnings("deprecation")
@ApiStatus.Internal
public class PolymerBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        var player = PacketContext.get();
        var state = PolymerBlockUtils.getPolymerBlockState(input, player);

        if (state != input) {
            var polyMap = Util.tryGetPolyMap(player.getPlayer());
            return polyMap.getClientState(state, player.getPlayer());
        } else {
            return state;
        }
    }
}
