package eu.pb4.polymer.core.impl.ui;

import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public class CreativeTabListUi extends MicroUi {
    private static final int ITEMS_PER_PAGE = 45;
    private final List<CreativeModeTab> items;

    private int page;

    public CreativeTabListUi(ServerPlayer player) {
        super(6);
        this.title(Component.literal("Creative Item Groups"));
        this.items = new ArrayList<>();
        this.items.addAll(PolymerCreativeModeTabUtils.getCreativeModeTabs(player));
        this.page = 0;
        this.drawUi();

        this.open(player);
    }

    private void drawUi() {
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min((page + 1) * ITEMS_PER_PAGE, this.items.size());
        for (int i = start; i < end; i++) {
            var itemGroup = this.items.get(i);
            var icon = itemGroup.getIconItem().copy();
            var text = itemGroup.getDisplayName().copy();
            if (!text.getStyle().isItalic()) {
                text.setStyle(text.getStyle().withItalic(false));
            }
            icon.set(DataComponents.CUSTOM_NAME, text);
            //icon.getOrCreateNbt().putInt("HideFlags", 255);
            this.slot(i - start, icon, (player, slotIndex, button, actionType) -> {
                playSound(player, SoundEvents.UI_BUTTON_CLICK);
                new CreativeTabUi(player, itemGroup);
            });
        }

        for (int i = Math.max(end - start, 0); i < ITEMS_PER_PAGE; i++) {
            this.slot(i, ItemStack.EMPTY, MicroUiElements.EMPTY_ACTION);
        }

        this.slot(ITEMS_PER_PAGE + 0, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);

        if (this.page == 0) {
            this.slot(ITEMS_PER_PAGE + 1, MicroUiElements.BUTTON_PREVIOUS_LOCK, MicroUiElements.EMPTY_ACTION);
        } else {
            this.slot(ITEMS_PER_PAGE + 1, MicroUiElements.BUTTON_PREVIOUS, (player, slotIndex, button, actionType) -> {
                CreativeTabListUi.this.page--;
                playSound(player, SoundEvents.UI_BUTTON_CLICK);
                this.drawUi();
            });
        }

        this.slot(ITEMS_PER_PAGE + 2, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 3, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 4, MicroUiElements.BUTTON_SEARCH, (player, slotIndex, button, actionType) -> {
            playSound(player, SoundEvents.UI_BUTTON_CLICK);
            new CreativeTabUi(player, CreativeModeTabs.searchTab());
        });
        this.slot(ITEMS_PER_PAGE + 5, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 6, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        if (this.page >= this.items.size() / ITEMS_PER_PAGE) {
            this.slot(ITEMS_PER_PAGE + 7, MicroUiElements.BUTTON_NEXT_LOCK, MicroUiElements.EMPTY_ACTION);
        } else {
            this.slot(ITEMS_PER_PAGE + 7, MicroUiElements.BUTTON_NEXT, (player, slotIndex, button, actionType) -> {
                CreativeTabListUi.this.page++;
                playSound(player, SoundEvents.UI_BUTTON_CLICK);
                this.drawUi();
            });
        }
        this.slot(ITEMS_PER_PAGE + 8, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
    }
}
