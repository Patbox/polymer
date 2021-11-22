package eu.pb4.polymer.impl.ui;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.utils.PolymerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.List;

public class CreativeTabListUi extends MicroUi {
    private static final int ITEMS_PER_PAGE = 45;
    private final List<PolymerItemGroup> items;

    private int page;

    public CreativeTabListUi(ServerPlayerEntity player) {
        super(6);
        this.title(new LiteralText("Polymer Item Groups"));
        this.items = PolymerUtils.getItemGroups(player);
        this.page = 0;
        this.drawUi();

        this.open(player);
    }

    private void drawUi() {
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min((page + 1) * ITEMS_PER_PAGE, this.items.size());
        for (int i = start; i < end; i++) {
            var itemGroup = this.items.get(i);
            this.slot(i - start, itemGroup.createIcon(), (player, slotIndex, button, actionType) -> {
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
                this.drawUi();
            });
        }

        this.slot(ITEMS_PER_PAGE + 2, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 3, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 4, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 5, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 6, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        if (this.page >= this.items.size() / ITEMS_PER_PAGE) {
            this.slot(ITEMS_PER_PAGE + 7, MicroUiElements.BUTTON_NEXT_LOCK, MicroUiElements.EMPTY_ACTION);
        } else {
            this.slot(ITEMS_PER_PAGE + 7, MicroUiElements.BUTTON_NEXT, (player, slotIndex, button, actionType) -> {
                CreativeTabListUi.this.page++;
                this.drawUi();
            });
        }
        this.slot(ITEMS_PER_PAGE + 8, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
    }
}
