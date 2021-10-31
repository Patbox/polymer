package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "disconnect", at = @At("TAIL"))
    private void polymer_clearRegistry(Text disconnectReason, CallbackInfo ci) {
        InternalClientRegistry.clear();
    }
}
