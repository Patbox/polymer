package eu.pb4.polymer.core.impl.compat.polymc;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class MiningCheck implements PolymerBlockUtils.MineEventListener {
    @Override
    public boolean onBlockMine(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        if (!Util.isPolyMapVanillaLike(player) || player.isCreative()) {
            return false;
        }

        var polyMap = PolyMapProvider.getPolyMap(player);
        return polyMap.getBlockPoly(state.getBlock()) != null || polyMap.getItemPoly(player.getMainHandStack().getItem()) != null;
    }
}
