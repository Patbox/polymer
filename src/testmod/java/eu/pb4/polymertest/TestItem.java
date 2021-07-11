package eu.pb4.polymertest;

import eu.pb4.polymer.item.BasicVirtualItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

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

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.sendMessage(new LiteralText("Use!" + hand), false);
        return super.use(world, user, hand);
    }
}
