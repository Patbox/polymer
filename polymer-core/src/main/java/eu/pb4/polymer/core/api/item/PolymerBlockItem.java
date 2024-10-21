package eu.pb4.polymer.core.api.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Basic implementation of PolymerItem for blocks
 */
public class PolymerBlockItem extends BlockItem implements PolymerItem {
    private final Item polymerItem;
    private final boolean polymerUseModel;

    public PolymerBlockItem(Block block, Settings settings) {
        this(block, settings, Items.TRIAL_KEY, true);
    }

    public PolymerBlockItem(Block block, Settings settings, Item polymerItem) {
        this(block, settings, polymerItem, false);
    }

    public PolymerBlockItem(Block block, Settings settings, Item polymerItem, boolean useModel) {
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
}
