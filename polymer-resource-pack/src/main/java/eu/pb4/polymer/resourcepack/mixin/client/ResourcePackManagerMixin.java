package eu.pb4.polymer.resourcepack.mixin.client;

import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import eu.pb4.polymer.resourcepack.mixin.accessors.VanillaResourcePackProviderAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(ResourcePackManager.class)
public abstract class ResourcePackManagerMixin<T extends ResourcePackProfile> {
    @Shadow
    @Final
    @Mutable
    private Set<ResourcePackProvider> providers;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void polymer_construct(ResourcePackProvider[] providers, CallbackInfo ci) {
        for (var x : providers) {
            if (x instanceof VanillaResourcePackProviderAccessor accessor && accessor.getType() == ResourceType.CLIENT_RESOURCES) {
                this.providers = new LinkedHashSet<>(this.providers);
                this.providers.add(new PolymerResourcePack.Provider());
            }
        }
    }
}
