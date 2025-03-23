package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class TestPickaxeItem extends Item implements VanillaModeledPolymerItem {

    public TestPickaxeItem(Item polymerItem, ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(settings.tool(material, BlockTags.PICKAXE_MINEABLE, attackDamage, attackSpeed, 0));
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.WOODEN_PICKAXE;
    }


    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return Identifier.of("polymertest", "pickaxe");
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        tooltip.add(0, Text.literal("Hello"));
        tooltip.add(Text.literal("World!"));
    }
}
