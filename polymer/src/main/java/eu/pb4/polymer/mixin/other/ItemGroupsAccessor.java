package eu.pb4.polymer.mixin.other;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemGroups.class)
public interface ItemGroupsAccessor {
    //@Mutable
    //@Accessor
    //static void setGROUPS(ItemGroup[] GROUPS) {
    //    throw new UnsupportedOperationException();
    //}

    @Accessor
    static ItemGroup getSEARCH() {
        throw new UnsupportedOperationException();
    }
}
