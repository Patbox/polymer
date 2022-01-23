package eu.pb4.polymer.mixin.compat;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.impl.interfaces.RegistryExtension;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(RegistrySyncManager.class)
public class fabricSync_RegistrySyncManagerMixin {

    @Redirect(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/registry/RegistryAttributeHolder;hasAttribute(Lnet/fabricmc/fabric/api/event/registry/RegistryAttribute;)Z", ordinal = 1), require = 0, remap = false)
    private static boolean polymer_skipRegistriesWithPolymerButNoMods(RegistryAttributeHolder instance, RegistryAttribute registryAttribute) {
        if (instance instanceof RegistryExtension reg) {
            return reg.polymer_getStatus() == RegistryExtension.Status.WITH_REGULAR_MODS;
        }

        return instance.hasAttribute(RegistryAttribute.MODDED);
    }

    @Redirect(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"), require = 0)
    private static Identifier polymer_skipVirtualObjects(Registry<Object> registry, Object obj, boolean isClientSync) {
        if (isClientSync
                && (obj instanceof PolymerObject
                        || (obj instanceof EntityType<?> type && PolymerEntityUtils.isRegisteredEntityType(type))
                        || (obj instanceof BlockEntityType<?> typeBE && PolymerBlockUtils.isRegisteredBlockEntity(typeBE))
                )
        ) {
            return null;
        } else {
            return registry.getId(obj);
        }
    }
}

