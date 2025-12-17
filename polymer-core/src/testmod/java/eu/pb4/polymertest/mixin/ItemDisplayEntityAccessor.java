package eu.pb4.polymertest.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.ItemDisplay.class)
public interface ItemDisplayEntityAccessor {
    @Accessor
    static EntityDataAccessor<ItemStack> getDATA_ITEM_STACK_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Byte> getDATA_ITEM_DISPLAY_ID() {
        throw new UnsupportedOperationException();
    }
}
