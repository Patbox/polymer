package eu.pb4.polymertest;

import eu.pb4.polymer.item.BasicVirtualItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;

import java.util.Random;

public class TestItem extends BasicVirtualItem {
    private Random random = new Random();
    public TestItem(Item.Settings settings, Item virtualItem) {
        super(settings, virtualItem);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        context.getWorld().setBlockState(context.getBlockPos(), Registry.BLOCK.getRandom(this.random).getDefaultState());
        return super.useOnBlock(context);
    }
}
