package eu.pb4.polymer.impl.ui;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class MicroUiElements {
    public static final MicroUi.PlayerClickAction EMPTY_ACTION = (player, slotIndex, button, actionType) -> { };

    public static final ItemStack EMPTY;
    public static final ItemStack BUTTON_PREVIOUS;
    public static final ItemStack BUTTON_PREVIOUS_LOCK;
    public static final ItemStack BUTTON_NEXT;
    public static final ItemStack BUTTON_NEXT_LOCK;
    public static final ItemStack BUTTON_BACK;
    public static final ItemStack BUTTON_SEARCH;

    static {
        EMPTY = Items.GRAY_STAINED_GLASS_PANE.getDefaultStack();
        EMPTY.setCustomName(LiteralText.EMPTY);

        BUTTON_PREVIOUS = Items.GREEN_STAINED_GLASS_PANE.getDefaultStack();
        BUTTON_PREVIOUS.setCustomName(new TranslatableText("createWorld.customize.custom.prev").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GREEN)));

        BUTTON_PREVIOUS_LOCK = Items.WHITE_STAINED_GLASS_PANE.getDefaultStack();
        BUTTON_PREVIOUS_LOCK.setCustomName(new TranslatableText("createWorld.customize.custom.prev").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.DARK_GRAY)));

        BUTTON_NEXT = Items.GREEN_STAINED_GLASS_PANE.getDefaultStack();
        BUTTON_NEXT.setCustomName(new TranslatableText("createWorld.customize.custom.next").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GREEN)));

        BUTTON_NEXT_LOCK = Items.WHITE_STAINED_GLASS_PANE.getDefaultStack();
        BUTTON_NEXT_LOCK.setCustomName(new TranslatableText("createWorld.customize.custom.next").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.DARK_GRAY)));

        BUTTON_BACK = Items.BARRIER.getDefaultStack();
        BUTTON_BACK.setCustomName(new TranslatableText("gui.back").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.RED)));

        BUTTON_SEARCH = Items.COMPASS.getDefaultStack();
        BUTTON_SEARCH.setCustomName(new TranslatableText("itemGroup.search").setStyle(Style.EMPTY.withItalic(false)));
    }
}
