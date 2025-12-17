package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Basic implementation of PolymerItem for blocks implementing PolymerHeadBlock
 */
public class PolymerHeadBlockItem extends BlockItem implements PolymerItem {
    private final PolymerHeadBlock polymerBlock;

    public <T extends Block & PolymerHeadBlock> PolymerHeadBlockItem(T block, Properties settings) {
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

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
        out.set(DataComponents.PROFILE, PolymerUtils.createProfileComponent(
                this.polymerBlock.getPolymerSkinValue(this.getBlock().defaultBlockState(), BlockPos.ZERO, context),
                this.polymerBlock.getPolymerSkinSignature(this.getBlock().defaultBlockState(), BlockPos.ZERO, context)
        ));
    }

    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context) {
        return PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
    }
}
