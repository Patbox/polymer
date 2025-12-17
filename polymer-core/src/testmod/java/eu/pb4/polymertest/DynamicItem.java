package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class DynamicItem extends Item implements PolymerItem {
    public DynamicItem(Properties settings) {
        super(settings);
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        user.displayClientMessage(Component.literal("Used!" + hand), false);
        return super.use(world, user, hand);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return itemStack.getOrDefault(TestMod.CLIENT_ITEM, Items.STICK);
    }
}
