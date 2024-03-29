package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Basic implementation of PolymerItem for blocks implementing PolymerHeadBlock
 */
public class PolymerHeadBlockItem extends BlockItem implements PolymerItem {
    private final PolymerHeadBlock polymerBlock;

    public <T extends Block & PolymerHeadBlock> PolymerHeadBlockItem(T block, Settings settings) {
        super(block, settings);
        this.polymerBlock = block;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.PLAYER_HEAD;
    }

    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipContext tooltipContext, ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipContext, player);
        out.getOrCreateNbt().put("SkullOwner", this.polymerBlock.getPolymerHeadSkullOwner(this.getBlock().getDefaultState(), BlockPos.ORIGIN, player));
        return out;
    }
}
