package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TestItem extends SimplePolymerItem {
    private RandomSource random = RandomSource.create();
    public TestItem(Item.Properties settings, Item virtualItem) {
        super(settings, virtualItem);
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        context.getLevel().setBlockAndUpdate(context.getClickedPos(), BuiltInRegistries.BLOCK.getRandom(this.random).get().value().defaultBlockState());
        return super.useOn(context);
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        user.displayClientMessage(Component.literal("Use!" + hand), false);
        return super.use(world, user, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        var builder = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            builder.append("I");
        }
        tooltipAdder.accept(Component.literal(builder.toString()));
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context) {
        var x = super.getPolymerItemStack(itemStack, tooltipType, context);
        x.set(DataComponents.RARITY, Rarity.EPIC);
        x.set(DataComponents.CONSUMABLE, new Consumable(context.getPlayer() != null && context.getPlayer().isCreative() ? Float.MAX_VALUE : 3, ItemUseAnimation.BOW,
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY), false, List.of()));
        return x;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
