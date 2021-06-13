package eu.pb4.polymertest;

import eu.pb4.polymer.item.VirtualItem;
import eu.pb4.polymer.resourcepack.CMDInfo;
import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;


public class TestBowItem extends BowItem implements VirtualItem {
    private final CMDInfo model;

    public TestBowItem(Settings settings, String model) {
        super(settings);

        this.model = ResourcePackUtils.requestCustomModelData(Items.BOW, new Identifier("polymertest", "item/" + model));
    }

    @Override
    public Item getVirtualItem() {
        return this.model.item();
    }

    @Override
    public ItemStack getVirtualItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        ItemStack out = VirtualItem.super.getVirtualItemStack(itemStack, player);
        out.getOrCreateTag().putInt("CustomModelData", this.model.value());
        return out;
    }
}
