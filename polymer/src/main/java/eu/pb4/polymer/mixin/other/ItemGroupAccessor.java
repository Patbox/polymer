package eu.pb4.polymer.mixin.other;

import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessor {
    //@Accessor
    //int getIndex();

    //@Mutable
    //@Accessor
    //void setIndex(int index);
}
