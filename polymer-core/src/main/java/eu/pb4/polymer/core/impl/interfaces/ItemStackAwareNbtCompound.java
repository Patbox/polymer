package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;

public interface ItemStackAwareNbtCompound {
    String MARKER_KEY = "$$polymer:itemstack";
    NbtElement MARKER_VALUE = NbtByte.of((byte) 1);
    default void polymerCore$setItemStack(boolean bool) {};
    default boolean polymerCore$getItemStack() {
        return false;
    };
}
