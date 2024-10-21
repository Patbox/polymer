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

    public PolymerSpawnEggItem(EntityType<? extends MobEntity> type, Settings settings) {
        this(type, Items.TRIAL_KEY, true, settings);
    }

    public PolymerSpawnEggItem(EntityType<? extends MobEntity> type, Item polymerItem, Settings settings) {
        this(type, polymerItem, false, settings);
    }
    public PolymerSpawnEggItem(EntityType<? extends MobEntity> type, Item polymerItem, boolean useModel, Settings settings) {
        super(type, 0, 0, settings);
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
