package eu.pb4.polymer.core.api.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymerSpawnEggItem extends SpawnEggItem implements PolymerItem {

    private final Item visualItem;

    public PolymerSpawnEggItem(EntityType<? extends MobEntity> type, Item visualItem, Settings settings) {
        super(type, 0, 0, settings);
        this.visualItem = visualItem;
    }


    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.visualItem;
    }
}
