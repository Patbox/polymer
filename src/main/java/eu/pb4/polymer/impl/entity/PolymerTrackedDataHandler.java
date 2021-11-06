package eu.pb4.polymer.impl.entity;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.mixin.ItemFrameEntityAccessor;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public interface PolymerTrackedDataHandler<T> extends TrackedDataHandler<T> {

    PolymerTrackedDataHandler<ItemStack> NAMELESS_ITEM_STACK = new PolymerTrackedDataHandler<>() {
        TrackedDataHandler<ItemStack> ITEM_FRAME_DEFAULT = ItemFrameEntityAccessor.getITEM_STACK().getType();

        @Override
        public TrackedDataHandler<ItemStack> getReal() {
            return ITEM_FRAME_DEFAULT;
        }

        public void write(PacketByteBuf buf, ItemStack stack) {
            var polymerStack = PolymerItemUtils.getPolymerItemStack(stack, PolymerUtils.getPlayer());

            if (!stack.hasCustomName() && !(stack.getItem() instanceof PolymerItem polymerItem && polymerItem.showDefaultNameInItemFrames())) {
                polymerStack.removeCustomName();
            }

            //buf.writeItemStack(stack);
            // Todo: remove that when STA is fixed
            if (polymerStack.isEmpty()) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                Item item = polymerStack.getItem();
                buf.writeVarInt(Item.getRawId(item));
                buf.writeByte(polymerStack.getCount());
                NbtCompound nbtCompound = null;
                if (item.isDamageable() || item.isNbtSynced()) {
                    nbtCompound = polymerStack.getNbt();
                }

                buf.writeNbt(nbtCompound);
            }
        }

        public ItemStack read(PacketByteBuf packetByteBuf) {
            return packetByteBuf.readItemStack();
        }

        public ItemStack copy(ItemStack itemStack) {
            return itemStack.copy();
        }
    };

    TrackedDataHandler<T> getReal();

    TrackedData<ItemStack> CUSTOM_ITEM_FRAME_STACK = new TrackedData<>(ItemFrameEntityAccessor.getITEM_STACK().getId(), NAMELESS_ITEM_STACK);
}
