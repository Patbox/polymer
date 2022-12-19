package eu.pb4.polymer.core.impl.ui;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;

public class CreativeTabUi extends MicroUi {
    private static final int ITEMS_PER_PAGE = 45;

    private final ItemGroup itemGroup;
    private final DefaultedList<ItemStack> items;
    private int page;

    public CreativeTabUi(ServerPlayerEntity player, ItemGroup itemGroup) {
        super(6);
        this.title(itemGroup.getDisplayName());
        this.itemGroup = itemGroup;
        this.items = DefaultedList.of();
        if (itemGroup == ItemGroups.getSearchGroup()) {
            var set = ItemStackSet.create();

            for (var group : PolymerItemGroupUtils.getItemGroups(player)) {
                set.addAll(PolymerItemGroupUtils.getContentsFor(player, group).search());
            }
            this.items.addAll(set);
        } else {
            this.items.addAll(PolymerItemGroupUtils.getContentsFor(player, itemGroup).main());
        }
        this.page = 0;
        this.drawUi();

        this.open(player);
    }

    private void drawUi() {
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min((page + 1) * ITEMS_PER_PAGE, this.items.size());
        for (int i = start; i < end; i++) {
            var stack = this.items.get(i);
            this.slot(i - start, stack, (player, slotIndex, button, actionType) -> {
                onMouseClick(stack, slotIndex, button, actionType, player);
            });
        }

        for (int i = Math.max(end - start, 0); i < ITEMS_PER_PAGE; i++) {
            this.slot(i, ItemStack.EMPTY, (player, slotIndex, button, actionType) -> {
                onMouseClick(ItemStack.EMPTY, slotIndex, button, actionType, player);
            });
        }

        this.slot(ITEMS_PER_PAGE + 0, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);

        if (this.page == 0) {
            this.slot(ITEMS_PER_PAGE + 1, MicroUiElements.BUTTON_PREVIOUS_LOCK, MicroUiElements.EMPTY_ACTION);
        } else {
            this.slot(ITEMS_PER_PAGE + 1, MicroUiElements.BUTTON_PREVIOUS, (player, slotIndex, button, actionType) -> {
                CreativeTabUi.this.page--;
                playSound(player, SoundEvents.UI_BUTTON_CLICK);
                this.drawUi();
            });
        }

        this.slot(ITEMS_PER_PAGE + 2, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 3, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 4, MicroUiElements.BUTTON_BACK, (player, slotIndex, button, actionType) -> {
            playSound(player, SoundEvents.UI_BUTTON_CLICK);
            new CreativeTabListUi(player);
        });
        this.slot(ITEMS_PER_PAGE + 5, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        this.slot(ITEMS_PER_PAGE + 6, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
        if (this.page >= this.items.size() / ITEMS_PER_PAGE) {
            this.slot(ITEMS_PER_PAGE + 7, MicroUiElements.BUTTON_NEXT_LOCK, MicroUiElements.EMPTY_ACTION);
        } else {
            this.slot(ITEMS_PER_PAGE + 7, MicroUiElements.BUTTON_NEXT, (player, slotIndex, button, actionType) -> {
                CreativeTabUi.this.page++;
                playSound(player, SoundEvents.UI_BUTTON_CLICK);
                this.drawUi();
            });
        }
        this.slot(ITEMS_PER_PAGE + 8, MicroUiElements.EMPTY, MicroUiElements.EMPTY_ACTION);
    }

    protected void onMouseClick(ItemStack itemStack, int slotId, int button, SlotActionType actionType, ServerPlayerEntity player) {
        boolean bl = actionType == SlotActionType.QUICK_MOVE;
        actionType = slotId == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType;

        var handler = player.currentScreenHandler;

        if (actionType != SlotActionType.QUICK_CRAFT) {
            ItemStack i = handler.getCursorStack();
            if (actionType == SlotActionType.SWAP) {
                if (!itemStack.isEmpty()) {
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.setCount(itemStack2.getMaxCount());
                    player.getInventory().setStack(button, itemStack2);
                    player.playerScreenHandler.sendContentUpdates();
                }

                return;
            }

            if (actionType == SlotActionType.CLONE) {
                if (handler.getCursorStack().isEmpty() && !itemStack.isEmpty()) {
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.setCount(itemStack2.getMaxCount());
                    handler.setCursorStack(itemStack2);
                }

                return;
            }

            if (actionType == SlotActionType.THROW) {
                if (!itemStack.isEmpty()) {
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.setCount(button == 0 ? 1 : itemStack2.getMaxCount());
                    player.dropItem(itemStack2, true);
                    //this.client.interactionManager.dropCreativeStack(itemStack2);
                }

                return;
            }

            if (!i.isEmpty() && !itemStack.isEmpty() && i.isItemEqual(itemStack) && ItemStack.areNbtEqual(i, itemStack)) {
                if (button == 0) {
                    if (bl) {
                        i.setCount(i.getMaxCount());
                    } else if (i.getCount() < i.getMaxCount()) {
                        i.increment(1);
                    }
                } else {
                    i.decrement(1);
                }
            } else if (!itemStack.isEmpty() && i.isEmpty()) {
                handler.setCursorStack(itemStack.copy());
                i = handler.getCursorStack();
                if (bl) {
                    i.setCount(i.getMaxCount());
                }
            } else if (button == 0) {
                handler.setCursorStack(ItemStack.EMPTY);
            } else {
                handler.getCursorStack().decrement(1);
            }
        }
    }
}
