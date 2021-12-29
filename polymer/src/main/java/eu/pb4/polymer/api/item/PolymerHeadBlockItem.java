package eu.pb4.polymer.api.item;

import eu.pb4.polymer.api.block.PolymerHeadBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Basic implementation of PolymerItem for blocks implementing PolymerHeadBlock
 */
public class PolymerHeadBlockItem extends BlockItem implements PolymerItem {
    private final PolymerHeadBlock polymerBlock;

    public PolymerHeadBlockItem(PolymerHeadBlock block, Settings settings) {
        super((Block) block, settings);
        this.polymerBlock = block;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.PLAYER_HEAD;
    }

    public ItemStack getPolymerItemStack(ItemStack itemStack, ServerPlayerEntity player) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, player);
        out.getOrCreateNbt().put("SkullOwner", this.polymerBlock.getPolymerHeadSkullOwner(this.getBlock().getDefaultState()));
        return out;
    }
}
