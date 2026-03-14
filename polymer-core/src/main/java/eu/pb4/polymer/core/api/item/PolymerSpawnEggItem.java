package eu.pb4.polymer.core.api.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class PolymerSpawnEggItem extends SpawnEggItem implements PolymerItem {

    private final Item polymerItem;
    private final boolean polymerUseModel;

    public PolymerSpawnEggItem(Item polymerItem, Properties settings) {
        this(polymerItem, false, settings);
    }
    public PolymerSpawnEggItem(Item polymerItem, boolean useModel, Properties settings) {
        super(settings);
        this.polymerItem = polymerItem;
        this.polymerUseModel = useModel;
    }


    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.polymerItem;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return this.polymerUseModel ? PolymerItem.super.getPolymerItemModel(stack, context, lookup) : null;
    }
}
