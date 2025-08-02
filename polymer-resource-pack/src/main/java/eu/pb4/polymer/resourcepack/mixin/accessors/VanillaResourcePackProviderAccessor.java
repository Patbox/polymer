package eu.pb4.polymer.resourcepack.mixin.accessors;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaResourcePackProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VanillaResourcePackProvider.class)
public interface VanillaResourcePackProviderAccessor {
    @Accessor
    ResourceType getType();
}
