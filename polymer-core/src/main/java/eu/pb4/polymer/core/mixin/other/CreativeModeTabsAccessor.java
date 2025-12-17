package eu.pb4.polymer.core.mixin.other;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

@Mixin(CreativeModeTabs.class)
public interface CreativeModeTabsAccessor {
    @Invoker
    static void callBuildAllTabContents(CreativeModeTab.ItemDisplayParameters x) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static CreativeModeTab.ItemDisplayParameters getCACHED_PARAMETERS() {
        throw new UnsupportedOperationException();
    }
}
