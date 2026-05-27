package eu.pb4.polymer.core.impl.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
        EMPTY = Items.STAINED_GLASS_PANE.gray().getDefaultInstance();

        BUTTON_PREVIOUS = Items.STAINED_GLASS_PANE.green().getDefaultInstance();
        BUTTON_PREVIOUS.set(DataComponents.CUSTOM_NAME, Component.translatable("spectatorMenu.previous_page").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GREEN)));

        BUTTON_PREVIOUS_LOCK = Items.STAINED_GLASS_PANE.white().getDefaultInstance();
        BUTTON_PREVIOUS_LOCK.set(DataComponents.CUSTOM_NAME, Component.translatable("spectatorMenu.previous_page").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.DARK_GRAY)));

        BUTTON_NEXT = Items.STAINED_GLASS_PANE.green().getDefaultInstance();
        BUTTON_NEXT.set(DataComponents.CUSTOM_NAME, Component.translatable("spectatorMenu.next_page").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GREEN)));

        BUTTON_NEXT_LOCK = Items.STAINED_GLASS_PANE.white().getDefaultInstance();
        BUTTON_NEXT_LOCK.set(DataComponents.CUSTOM_NAME, Component.translatable("spectatorMenu.next_page").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.DARK_GRAY)));

        BUTTON_BACK = Items.BARRIER.getDefaultInstance();
        BUTTON_BACK.set(DataComponents.CUSTOM_NAME, Component.translatable("gui.back").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.RED)));

        BUTTON_SEARCH = Items.COMPASS.getDefaultInstance();
        BUTTON_SEARCH.set(DataComponents.CUSTOM_NAME, Component.translatable("itemGroup.search").setStyle(Style.EMPTY.withItalic(false)));
    }
}
