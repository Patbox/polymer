package eu.pb4.polymer.item;

import eu.pb4.polymer.block.VirtualHeadBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class VirtualHeadBlockItem extends BlockItem implements VirtualItem {
    private final VirtualHeadBlock virtualBlock;

    public VirtualHeadBlockItem(VirtualHeadBlock block, Settings settings) {
        super((Block) block, settings);
        this.virtualBlock = block;
    }

    @Override
    public Item getVirtualItem() {
        return Items.PLAYER_HEAD;
    }

    public ItemStack getVirtualItemStack(ItemStack itemStack) {
        ItemStack out = VirtualItem.super.getVirtualItemStack(itemStack);
        out.getOrCreateTag().put("SkullOwner", this.virtualBlock.getVirtualHeadSkullOwner(this.getBlock().getDefaultState()));
        return out;
    }

    public void clearVirtualNBT(ItemStack itemStack) {
        itemStack.getOrCreateTag().remove("SkullOwner");
    }
}
