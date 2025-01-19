package eu.pb4.polymer.core.impl.compat.polymc;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

public class PolyMcUtils {

    public static BlockState toVanilla(BlockState state, ServerPlayerEntity player) {
        if (CompatStatus.POLYMC) {
            return PolyMapProvider.getPolyMap(player).getClientState(state, player);
        }

        return state;
    }

    public static ItemStack toVanilla(ItemStack stack, ServerPlayerEntity player) {
        if (CompatStatus.POLYMC && !stack.isEmpty()) {
            return PolyMapProvider.getPolyMap(player).getClientItem(stack, player, ItemLocation.INVENTORY);
        }

        return stack;
    }

    public static boolean isServerSide(Registry reg, Object obj) {
        return !reg.getId(obj).getNamespace().equals("minecraft");
    }

    public static void register() {
        if (CompatStatus.POLYMC && PolymerImpl.OVERRIDE_POLYMC_MINING) {
            var event = new PolymcInteractionReplacement();
            PolymerBlockUtils.SERVER_SIDE_MINING_CHECK.register(event);
            PolymerBlockUtils.POLYMER_BLOCK_INTERACTION_CHECK.register(event);
            PolymerItemUtils.POLYMER_ITEM_INTERACTION_CHECK.register(event);
        }
    }
}
