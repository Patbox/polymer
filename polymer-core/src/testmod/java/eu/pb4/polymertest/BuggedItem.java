package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;


public class BuggedItem extends Item implements PolymerItem {

    public BuggedItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        //if(!PolymerResourcePackUtils.hasPack(player)) return vanillaItem;
        return Items.SHIELD;
    }
}