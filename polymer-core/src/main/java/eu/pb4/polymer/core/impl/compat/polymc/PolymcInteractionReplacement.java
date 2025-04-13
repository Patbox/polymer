package eu.pb4.polymer.core.impl.compat.polymc;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymcInteractionReplacement implements PolymerBlockUtils.MineEventListener, PolymerBlockUtils.PolymerBlockInteractionListener, PolymerItemUtils.PolymerItemInteractionListener, PolymerItemUtils.ServerItemPredicate {
    @Override
    public boolean onBlockMine(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return !player.isCreative() && (isPolyMcBlock(state, player) || isPolyMcItem(player.getMainHandStack(), player));
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, BlockHitResult blockHitResult, ActionResult actionResult) {
        return isPolyMcBlock(state, player) || isPolyMcItem(stack, player);
    }

    @Override
    public boolean isPolymerItemInteraction(ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, ActionResult actionResult) {
        return isPolyMcItem(stack, player);
    }

    private boolean isPolyMcItem(ItemStack itemStack, ServerPlayerEntity player) {
        if (!Util.isPolyMapVanillaLike(player) ) {
            return false;
        }

        var polyMap = PolyMapProvider.getPolyMap(player);
        var tool = polyMap.getItemPoly(itemStack.getItem());
        return (tool != null && !(tool instanceof PassthroughPoly));
    }

    private boolean isPolyMcBlock(BlockState state, ServerPlayerEntity player) {
        if (!Util.isPolyMapVanillaLike(player)) {
            return false;
        }

        var polyMap = PolyMapProvider.getPolyMap(player);
        var block = polyMap.getBlockPoly(state.getBlock());
        return (block != null && !(block instanceof PassthroughPoly));
    }

    @Override
    public boolean isServerItem(ItemStack itemStack, PacketContext context) {
        if (!Util.isPolyMapVanillaLike(context.getPlayer() ) ) {
            return false;
        }

        var polyMap = Util.tryGetPolyMap(context.getClientConnection());
        var tool = polyMap.getItemPoly(itemStack.getItem());
        return (tool != null && !(tool instanceof PassthroughPoly));
    }
}
