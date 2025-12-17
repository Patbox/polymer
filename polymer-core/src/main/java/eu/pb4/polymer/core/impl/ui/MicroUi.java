package eu.pb4.polymer.core.impl.ui;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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
    private Component title = Component.empty();
    private final MenuType<?> type;
    protected final int size;

    public MicroUi(int lines) {
        this.size = lines * 9;
        this.type = switch (lines) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
        this.elements = new UiElement[this.size];
    }

    public MicroUi title(Component title) {
        this.title = title;
        return this;
    }

    public MicroUi slot(int index, ItemStack stack, PlayerClickAction action) {
        this.elements[index] = new UiElement(stack, action);
        return this;
    }

    protected void tick() {}

    public void open(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return MicroUi.this.title;
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
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

    public static void playSound(ServerPlayer player, Holder<SoundEvent> soundEvent) {
        playSound(player, soundEvent.value());
    }
    public static void playSound(ServerPlayer player, SoundEvent soundEvent) {
        player.connection.send(new ClientboundSoundEntityPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), SoundSource.MASTER, player,  0.2f, 1,
                player.getRandom().nextLong()
        ));
    }

    @FunctionalInterface
    public interface PlayerClickAction {
        PlayerClickAction NOOP = (a, b, c, d) -> {};
        void onClick(ServerPlayer player, int slotIndex, int button, ClickType actionType);
    }

    private record UiElement(ItemStack stack, PlayerClickAction action) {
    }

    private class InternalScreenHandler extends AbstractContainerMenu {
        protected InternalScreenHandler(int syncId, Inventory playerInventory, Player player) {
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
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
            if (slotIndex > -1 && slotIndex < MicroUi.this.size) {
                var slot = MicroUi.this.elements[slotIndex];
                if (slot != null) {
                    slot.action().onClick((ServerPlayer) player, slotIndex, button, actionType);
                }
                ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(this.containerId, 0, slotIndex, this.getSlot(slotIndex).getItem()));
                ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(-1, 0, 0, this.getCarried()));
            } else if (actionType != ClickType.QUICK_MOVE) {
                super.clicked(slotIndex, button, actionType, player);
            }
        }

        @Override
        public void broadcastChanges() {
            MicroUi.this.tick();
            super.broadcastChanges();
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }
    }

    private record InternalInventory(MicroUi ui) implements Container {
        @Override
        public int getContainerSize() {
            return ui.size;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int slot) {
            return ui.elements[slot] != null ? ui.elements[slot].stack : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {

        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {

        }
    }
}
