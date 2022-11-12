package eu.pb4.polymertest;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.api.utils.PolymerKeepModel;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;


public class TestClientBlockItem extends BlockItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel {
    public TestClientBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this;
    }
}
