package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class TestScrollableItem extends SimplePolymerItem {
    int previous = -1;
    int total = 0;

    public TestScrollableItem(Properties settings) {
        super(settings, /*PolymerTagUtils.enableAndGetFakeBundleItem()*/ Items.MUSIC_DISC_CHIRP);
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return null;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        user.sendSystemMessage(Component.literal("Use!" + hand));
        return super.use(world, user, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        super.modifyBasePolymerItemStack(out, stack, context, lookup);
        out.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(
                new ItemStackTemplate(Items.STONE),
                new ItemStackTemplate(Items.STONE),
                new ItemStackTemplate(Items.STONE),
                new ItemStackTemplate(Items.STONE),
                new ItemStackTemplate(Items.STONE),
                new ItemStackTemplate(Items.STONE),
                new ItemStackTemplate(Items.STONE),
                new ItemStackTemplate(Items.STONE),
                new ItemStackTemplate(Items.STONE)
        )));

        //out.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, ReferenceLinkedOpenHashSet.of(DataComponents.BUNDLE_CONTENTS)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public void handleScroll(ItemStack stack, int selectedItem) {
        if (selectedItem == -1) {
            previous = -1;
            System.out.println("Dir=" + 0 + ", sel=" + selectedItem + ", total=" + total);
            return;
        }

        var dir = 0;

        dir = switch (previous) {
            case -1, 0, 8 -> selectedItem > 4 ? 1 : -1;
            default -> Mth.sign(previous - selectedItem);
        };

        total += dir;
        previous = selectedItem;

        System.out.println("Dir=" + dir + ", sel=" + selectedItem + ", total=" + total);
    }
}
