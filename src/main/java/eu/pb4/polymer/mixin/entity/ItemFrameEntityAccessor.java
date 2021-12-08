package eu.pb4.polymer.mixin.entity;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrameEntity.class)
public interface ItemFrameEntityAccessor {
    @Accessor
    static TrackedData<ItemStack> getITEM_STACK() {
        throw new UnsupportedOperationException();
    }
}
