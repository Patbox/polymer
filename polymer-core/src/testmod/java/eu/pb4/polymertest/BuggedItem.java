package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;


public class BuggedItem extends Item implements VanillaModeledPolymerItem {

    public BuggedItem(Properties settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        //if(!PolymerResourcePackUtils.hasPack(player)) return vanillaItem;
        return Items.SHIELD;
    }
}