package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.core.HolderLookup;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClickItem extends SimplePolymerItem {

    private final BiConsumer<ServerPlayer, InteractionHand> executor;

    public ClickItem(Properties settings, Item virtualItem, BiConsumer<ServerPlayer, InteractionHand> executor) {
        super(settings, virtualItem);
        this.executor = executor;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return null;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user instanceof ServerPlayer player) {
            this.executor.accept(player, hand);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
