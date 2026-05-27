package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class TestHelmetItem extends Item implements VanillaModeledPolymerItem {
    public TestHelmetItem(Properties settings) {
        super(settings.humanoidArmor(ArmorMaterials.IRON, ArmorType.HELMET));
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.STAINED_GLASS.white();
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup) {
        var x = VanillaModeledPolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
        x.set(DataComponents.RARITY, Rarity.EPIC);
        return x;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        user.sendSystemMessage(Component.literal("Use!" + hand));
        return super.use(world, user, hand);
    }
}
