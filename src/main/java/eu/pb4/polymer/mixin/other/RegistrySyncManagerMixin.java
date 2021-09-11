package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.interfaces.VirtualObject;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Pseudo
@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin {

    @Redirect(method = "toTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"))
    private static Identifier skipVirtualObjects(Registry<Object> registry, Object obj, boolean isClientSync) {
        if (isClientSync && obj instanceof VirtualObject) {
            return null;
        } else {
            return registry.getId(obj);
        }
    }
}

