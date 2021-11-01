package eu.pb4.polymer.mixin.compat;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.utils.PolymerObject;
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

    @Redirect(method = "toTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"), require = 0)
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

