package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;


public class TestBowItem extends BowItem implements PolymerItem {
    public TestBowItem(Properties settings, String model) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.BOW;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return Identifier.fromNamespaceAndPath("polymertest", "bow");
    }
}
