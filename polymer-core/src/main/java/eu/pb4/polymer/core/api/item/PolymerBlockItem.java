package eu.pb4.polymer.core.api.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Basic implementation of PolymerItem for blocks
 */
public class PolymerBlockItem extends BlockItem implements PolymerItem {
    private final Item polymerItem;

    public PolymerBlockItem(Block block, Settings settings, Item virtualItem) {
        super(block, settings);
        this.polymerItem = virtualItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.polymerItem;
    }
}
