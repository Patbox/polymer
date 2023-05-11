package eu.pb4.polymer.core.mixin.other;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ItemGroups.class)
public interface ItemGroupsAccessor {
    @Invoker
    static void callUpdateEntries(ItemGroup.DisplayContext x) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static ItemGroup.DisplayContext getDisplayContext() {
        throw new UnsupportedOperationException();
    }
}
