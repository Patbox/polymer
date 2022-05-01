package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.impl.PolymerImpl;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientBuiltinResourcePackProvider.class)
public class ClientBuiltinResourcePackProviderMixin {
    @ModifyArg(method = "loadServerPack", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackProfile;<init>(Ljava/lang/String;ZLjava/util/function/Supplier;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/resource/ResourcePackCompatibility;Lnet/minecraft/resource/ResourcePackProfile$InsertionPosition;ZLnet/minecraft/resource/ResourcePackSource;)V"), index = 1)
    private boolean polymer_unlockPack(boolean locked) {
        return PolymerImpl.UNLOCK_SERVER_PACK_CLIENT ? false : locked;
    }
}
