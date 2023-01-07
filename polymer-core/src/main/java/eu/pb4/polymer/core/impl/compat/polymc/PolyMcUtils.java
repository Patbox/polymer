package eu.pb4.polymer.core.impl.compat.polymc;

import eu.pb4.polymer.common.impl.CompatStatus;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class PolyMcUtils {

    public static BlockState toVanilla(BlockState state, ServerPlayerEntity player) {
        if (CompatStatus.POLYMC) {
            return PolyMapProvider.getPolyMap(player).getClientState(state, player);
        }

        return state;
    }

    public static ItemStack toVanilla(ItemStack stack, ServerPlayerEntity player) {
        if (CompatStatus.POLYMC) {
            return PolyMapProvider.getPolyMap(player).getClientItem(stack, player, ItemLocation.INVENTORY);
        }

        return stack;
    }
}
