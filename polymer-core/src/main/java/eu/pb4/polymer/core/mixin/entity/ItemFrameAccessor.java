package eu.pb4.polymer.core.mixin.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrame.class)
public interface ItemFrameAccessor {
    @Accessor
    static EntityDataAccessor<ItemStack> getDATA_ITEM() {
        throw new UnsupportedOperationException();
    }
}
