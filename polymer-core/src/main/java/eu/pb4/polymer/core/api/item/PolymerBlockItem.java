package eu.pb4.polymer.core.api.item;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Basic implementation of PolymerItem for blocks
 */
public class PolymerBlockItem extends BlockItem implements PolymerItem {
    private final Item polymerItem;
    private final boolean polymerUseModel;

    public PolymerBlockItem(Block block, Properties settings) {
        this(block, settings, Items.TRIAL_KEY, true);
    }

    public PolymerBlockItem(Block block, Properties settings, Item polymerItem) {
        this(block, settings, polymerItem, false);
    }

    public PolymerBlockItem(Block block, Properties settings, Item polymerItem, boolean useModel) {
        super(block, settings);
        this.polymerItem = polymerItem;
        this.polymerUseModel = useModel;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.polymerItem;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.polymerUseModel ? PolymerItem.super.getPolymerItemModel(stack, context) : null;
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return true;
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
