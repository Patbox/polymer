package eu.pb4.polymer.impl.ui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;


/**
 * If you want to create ui in your mod you should just use sgui library instead!
 * It's more complete and has more functionality!
 *
 * This one is just simple util which most likely will be used only
 * for creative players and admins
 */
public class MicroUi {
    private final UiElement[] elements;
    private final int lines;
    private Text title = LiteralText.EMPTY;
    private final ScreenHandlerType<?> type;
    protected final int size;

    public MicroUi(int lines) {
        this.size = lines * 9;
        this.lines = lines;
        this.type = switch (lines) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
        this.elements = new UiElement[this.size];
    }

    public MicroUi title(Text title) {
        this.title = title;
        return this;
    }

    public MicroUi slot(int index, ItemStack stack, PlayerClickAction action) {
        this.elements[index] = new UiElement(stack, action);
        return this;
    }

    protected void tick() {}

    public void open(ServerPlayerEntity player) {
        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return MicroUi.this.title;
            }

            @Nullable
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new InternalScreenHandler(syncId, inv, player);
            }
        });
    }

    public MicroUi slot(int index, ItemStack stack) {
        return slot(index, stack, PlayerClickAction.NOOP);
    }

    public MicroUi clear() {
        for (int i = 0; i < this.size; i++) {
            this.elements[i] = null;
        }
        return this;
    }

    @FunctionalInterface
    public interface PlayerClickAction {
        PlayerClickAction NOOP = (a, b, c, d) -> {};
        void onClick(ServerPlayerEntity player, int slotIndex, int button, SlotActionType actionType);
    }

    private record UiElement(ItemStack stack, PlayerClickAction action) {
    }

    private class InternalScreenHandler extends ScreenHandler {
        protected InternalScreenHandler(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            super(MicroUi.this.type, syncId);

            var inv = new InternalInventory(MicroUi.this);
            for (int slot = 0; slot < MicroUi.this.size; slot++) {
                this.addSlot(new Slot(inv, slot, 0, 0));
            }

            for(int i = 0; i < 3; ++i) {
                for(int j = 0; j < 9; ++j) {
                    this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
                }
            }

            for(int i = 0; i < 9; ++i) {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
            }
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

        @Override
        public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
            if (slotIndex > -1 && slotIndex < MicroUi.this.size) {
                var slot = MicroUi.this.elements[slotIndex];
                if (slot != null) {
                    slot.action().onClick((ServerPlayerEntity) player, slotIndex, button, actionType);
                }
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, 0, slotIndex, this.getSlot(slotIndex).getStack()));
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, 0, 0, this.getCursorStack()));
            } else if (actionType != SlotActionType.QUICK_MOVE) {
                super.onSlotClick(slotIndex, button, actionType, player);
            }
        }

        @Override
        public void sendContentUpdates() {
            MicroUi.this.tick();
            super.sendContentUpdates();
        }
    }

    private record InternalInventory(MicroUi ui) implements Inventory {
        @Override
        public int size() {
            return ui.size;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getStack(int slot) {
            return ui.elements[slot] != null ? ui.elements[slot].stack : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {

        }

        @Override
        public void markDirty() {

        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {

        }
    }
}
