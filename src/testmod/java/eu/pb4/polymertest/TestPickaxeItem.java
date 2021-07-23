package eu.pb4.polymertest;

import eu.pb4.polymer.item.VirtualItem;
import eu.pb4.polymer.resourcepack.CMDInfo;
import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestPickaxeItem extends PickaxeItem implements VirtualItem {
    private final CMDInfo model = ResourcePackUtils.requestCustomModelData(Items.WOODEN_PICKAXE, new Identifier("polymertest", "item/pickaxe"));

    public TestPickaxeItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public Item getVirtualItem() {
        return this.model.item();
    }

    @Override
    public void modifyTooltip(List<Text> tooltip, ItemStack stack, ServerPlayerEntity player) {
        tooltip.add(0, new LiteralText("Hello"));
        tooltip.add(new LiteralText("World!"));
    }

    @Override
    public int getCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return model.value();
    }
}
