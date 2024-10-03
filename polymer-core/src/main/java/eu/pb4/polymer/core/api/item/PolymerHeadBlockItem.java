package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.block.Block;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Basic implementation of PolymerItem for blocks implementing PolymerHeadBlock
 */
public class PolymerHeadBlockItem extends BlockItem implements PolymerItem {
    private final PolymerHeadBlock polymerBlock;

    public <T extends Block & PolymerHeadBlock> PolymerHeadBlockItem(T block, Settings settings) {
        super(block, settings);
        this.polymerBlock = block;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.PLAYER_HEAD;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }

    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context) {
        ItemStack out = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);

        out.set(DataComponentTypes.PROFILE, PolymerUtils.createProfileComponent(
                this.polymerBlock.getPolymerSkinValue(this.getBlock().getDefaultState(), BlockPos.ORIGIN, context),
                this.polymerBlock.getPolymerSkinSignature(this.getBlock().getDefaultState(), BlockPos.ORIGIN, context)
        ));
        return out;
    }
}
