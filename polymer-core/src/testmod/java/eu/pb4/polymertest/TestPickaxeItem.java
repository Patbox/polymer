package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class TestPickaxeItem extends Item implements VanillaModeledPolymerItem {

    public TestPickaxeItem(Item polymerItem, ToolMaterial material, int attackDamage, float attackSpeed, Properties settings) {
        super(settings.tool(material, BlockTags.MINEABLE_WITH_PICKAXE, attackDamage, attackSpeed, 0));
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.WOODEN_PICKAXE;
    }


    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return Identifier.fromNamespaceAndPath("polymertest", "pickaxe");
    }

    @Override
    public void modifyClientTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
        tooltip.add(0, Component.literal("Hello"));
        tooltip.add(Component.literal("World!"));
    }
}
