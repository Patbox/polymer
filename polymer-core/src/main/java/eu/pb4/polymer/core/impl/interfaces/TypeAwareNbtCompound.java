package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public interface TypeAwareNbtCompound {
    String MARKER_KEY = "$$polymer:type";

    NbtString STACK_TYPE = NbtString.of( "item_stack");
    NbtString STATE_TYPE = NbtString.of("block_state");
    default void polymerCore$setType(NbtString type) {};
    default NbtString polymerCore$getType() {
        return null;
    };
}
