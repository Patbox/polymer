package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class TestPickaxeItem extends PickaxeItem implements VanillaModeledPolymerItem {

    public TestPickaxeItem(Item polymerItem, ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
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
