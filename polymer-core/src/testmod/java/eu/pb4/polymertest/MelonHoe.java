package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public class MelonHoe extends HoeItem implements PolymerItem {
    public static final ToolMaterial MELON_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3047, 1F, 5.0F, 22,
            TagKey.of(RegistryKeys.ITEM, Identifier.of("melon")));

    public MelonHoe(Settings settings) {
        super(MELON_MATERIAL, -2, 0, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.DIAMOND_HOE;
    }
}