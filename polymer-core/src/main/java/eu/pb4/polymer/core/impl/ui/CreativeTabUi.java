package eu.pb4.polymer.core.impl.ui;

import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

public class CreativeTabUi extends MicroUi {
    private static final int ITEMS_PER_PAGE = 45;

    private final CreativeModeTab itemGroup;
    private final NonNullList<ItemStack> items;
    private int page;

    public CreativeTabUi(ServerPlayer player, CreativeModeTab itemGroup) {
        super(6);
        this.title(itemGroup.getDisplayName());
        this.itemGroup = itemGroup;
        this.items = NonNullList.create();
        if (itemGroup == CreativeModeTabs.searchTab()) {
            var set = ItemStackLinkedSet.createTypeAndComponentsSet();

            for (var group : PolymerCreativeModeTabUtils.getCreativeModeTabs(player)) {
                set.addAll(PolymerCreativeModeTabUtils.getContentsFor(player, group).search());
            }
            this.items.addAll(set);
        } else {
            this.items.addAll(PolymerCreativeModeTabUtils.getContentsFor(player, itemGroup).main());
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

    protected void onMouseClick(ItemStack itemStack, int slotId, int button, ContainerInput actionType, ServerPlayer player) {
        boolean bl = actionType == ContainerInput.QUICK_MOVE;
        actionType = slotId == -999 && actionType == ContainerInput.PICKUP ? ContainerInput.THROW : actionType;

        var handler = player.containerMenu;

        if (actionType != ContainerInput.QUICK_CRAFT) {
            ItemStack i = handler.getCarried();
            if (actionType == ContainerInput.SWAP) {
                if (!itemStack.isEmpty()) {
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.setCount(itemStack2.getMaxStackSize());
                    player.getInventory().setItem(button, itemStack2);
                    player.inventoryMenu.broadcastChanges();
                }

                return;
            }

            if (actionType == ContainerInput.CLONE) {
                if (handler.getCarried().isEmpty() && !itemStack.isEmpty()) {
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.setCount(itemStack2.getMaxStackSize());
                    handler.setCarried(itemStack2);
                }

                return;
            }

            if (actionType == ContainerInput.THROW) {
                if (!itemStack.isEmpty()) {
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.setCount(button == 0 ? 1 : itemStack2.getMaxStackSize());
                    player.drop(itemStack2, true);
                    //this.client.interactionManager.dropCreativeStack(itemStack2);
                }

                return;
            }

            if (!i.isEmpty() && !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, i)) {
                if (button == 0) {
                    if (bl) {
                        i.setCount(i.getMaxStackSize());
                    } else if (i.getCount() < i.getMaxStackSize()) {
                        i.grow(1);
                    }
                } else {
                    i.shrink(1);
                }
            } else if (!itemStack.isEmpty() && i.isEmpty()) {
                handler.setCarried(itemStack.copy());
                i = handler.getCarried();
                if (bl) {
                    i.setCount(i.getMaxStackSize());
                }
            } else if (button == 0) {
                handler.setCarried(ItemStack.EMPTY);
            } else {
                handler.getCarried().shrink(1);
            }
        }
    }
}
