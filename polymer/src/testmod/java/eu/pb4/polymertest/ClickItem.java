package eu.pb4.polymertest;

import eu.pb4.polymer.api.item.SimplePolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.function.BiConsumer;

public class ClickItem extends SimplePolymerItem {

    private final BiConsumer<ServerPlayerEntity, Hand> executor;

    public ClickItem(Settings settings, Item virtualItem, BiConsumer<ServerPlayerEntity, Hand> executor) {
        super(settings, virtualItem);
        this.executor = executor;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            this.executor.accept(player, hand);
        }
        return TypedActionResult.success(user.getStackInHand(hand), true);
    }
}
