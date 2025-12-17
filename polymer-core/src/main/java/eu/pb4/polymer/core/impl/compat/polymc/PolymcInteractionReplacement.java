package eu.pb4.polymer.core.impl.compat.polymc;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymcInteractionReplacement implements PolymerBlockUtils.MineEventListener, PolymerBlockUtils.PolymerBlockInteractionListener, PolymerItemUtils.PolymerItemInteractionListener, PolymerItemUtils.ServerItemPredicate {
    @Override
    public boolean onBlockMine(BlockState state, BlockPos pos, ServerPlayer player) {
        return !player.isCreative() && (isPolyMcBlock(state, player) || isPolyMcItem(player.getMainHandItem(), player));
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return isPolyMcBlock(state, player) || isPolyMcItem(stack, player);
    }

    @Override
    public boolean isPolymerItemInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, InteractionResult actionResult) {
        return isPolyMcItem(stack, player);
    }

    private boolean isPolyMcItem(ItemStack itemStack, ServerPlayer player) {
        if (!Util.isPolyMapVanillaLike(player) ) {
            return false;
        }

        var polyMap = PolyMapProvider.getPolyMap(player);
        var tool = polyMap.getItemPoly(itemStack.getItem());
        return (tool != null && !(tool instanceof PassthroughPoly));
    }

    private boolean isPolyMcBlock(BlockState state, ServerPlayer player) {
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
