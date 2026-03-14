package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class MelonHoe extends HoeItem implements PolymerItem {
    public static final ToolMaterial MELON_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3047, 1F, 5.0F, 22,
            TagKey.create(Registries.ITEM, Identifier.parse("melon")));

    public MelonHoe(Properties settings) {
        super(MELON_MATERIAL, -2, 0, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.DIAMOND_HOE;
    }
}