package eu.pb4.polymer.core.api.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymerSpawnEggItem extends SpawnEggItem implements PolymerItem {

    private final Item polymerItem;
    private final boolean polymerUseModel;

    public PolymerSpawnEggItem(Item polymerItem, Settings settings) {
        this(polymerItem, false, settings);
    }
    public PolymerSpawnEggItem(Item polymerItem, boolean useModel, Settings settings) {
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
