package eu.pb4.polymer.common.mixin;

import net.minecraft.core.RegistrySetBuilder;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.data.registries.VanillaRegistries.class)
public interface VanillaRegistriesAccessor {
    @Accessor
    static RegistrySetBuilder getWORLD_BUILDER() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static RegistrySetBuilder getRELOADABLE_BUILDER() {
        throw new UnsupportedOperationException();
    }
}
