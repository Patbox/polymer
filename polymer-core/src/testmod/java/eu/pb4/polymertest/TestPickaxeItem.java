package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestPickaxeItem extends PickaxeItem implements PolymerItem {
    private final PolymerModelData model;

    public TestPickaxeItem(Item polymerItem, ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, settings);
        this.model = PolymerResourcePackUtils.requestModel(polymerItem, new Identifier("polymertest", "item/pickaxe"));
    }

    @Override
    public Item getPolymerItem(ItemStack stack, ServerPlayerEntity player) {
        return this.model.item();
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, ServerPlayerEntity player) {
        tooltip.add(0, Text.literal("Hello"));
        tooltip.add(Text.literal("World!"));
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return model.value();
    }
}
