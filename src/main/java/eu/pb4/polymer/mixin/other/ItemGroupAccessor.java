package eu.pb4.polymer.mixin.other;

import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessor {
    @Accessor
    static ItemGroup[] getGROUPS() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor
    static void setGROUPS(ItemGroup[] GROUPS) {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor
    void setIndex(int index);

    @Accessor
    String getId();
}
