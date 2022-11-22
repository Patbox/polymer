package eu.pb4.polymer.rsm.mixin.fabric;

import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(RegistrySyncManager.class)
public class fabricSync_RegistrySyncManagerMixin {

    @Redirect(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/registry/RegistryAttributeHolder;hasAttribute(Lnet/fabricmc/fabric/api/event/registry/RegistryAttribute;)Z", ordinal = 1), require = 0, remap = false)
    private static boolean polymer_registry_sync$skipRegistryWithoutModded(RegistryAttributeHolder instance, RegistryAttribute registryAttribute) {
        if (instance instanceof RegistrySyncExtension reg) {
            return reg.polymer_registry_sync$getStatus() == RegistrySyncExtension.Status.WITH_MODDED;
        }

        return instance.hasAttribute(RegistryAttribute.MODDED);
    }

    @Redirect(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"), require = 0)
    private static Identifier polymer_registry_sync$skipServerEntries(Registry<Object> registry, Object obj, boolean isClientSync) {
        if (isClientSync && RegistrySyncUtils.isServerEntry(registry, obj)) {
            return null;
        } else {
            return registry.getId(obj);
        }
    }
}

