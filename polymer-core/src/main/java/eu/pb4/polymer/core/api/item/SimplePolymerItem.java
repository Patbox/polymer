package eu.pb4.polymer.core.api.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Basic implementation of PolymerItem
 */
public class SimplePolymerItem extends Item implements PolymerItem {
    private final Item polymerItem;
    private final boolean polymerUseModel;

    public SimplePolymerItem(Settings settings) {
        this(settings, Items.TRIAL_KEY, true);
    }

    public SimplePolymerItem(Settings settings, Item polymerItem) {
        this(settings, polymerItem, false);
    }

    public SimplePolymerItem(Settings settings, Item polymerItem, boolean useModel) {
        super(settings);
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
