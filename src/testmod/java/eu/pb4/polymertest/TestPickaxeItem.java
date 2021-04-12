package eu.pb4.polymertest;

import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.List;

public class TestPickaxeItem extends PickaxeItem implements VirtualItem {
    public TestPickaxeItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public Item getVirtualItem() {
        return Items.WOODEN_PICKAXE;
    }

    @Override
    public void addTextToTooltip(List<Text> tooltip, ItemStack stack, ServerPlayerEntity player) {
        tooltip.add(0, new LiteralText("Hello"));
        tooltip.add(new LiteralText("World!"));
    }
}
