package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.NetworkHandlerExtension;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin implements NetworkHandlerExtension {
    @Shadow @Final protected ClientConnection connection;

    @Shadow @Final protected MinecraftClient client;

    @Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V", at = @At("HEAD"), cancellable = true)
    private void polymerNet$catchPackets(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (ClientPacketRegistry.handle(this.client, (ClientCommonNetworkHandler) (Object) this, packet.payload())) {
            ci.cancel();
        }
    }

    @Override
    public long polymerNet$lastPacketUpdate(Identifier identifier) {
        return 0;
    }

    @Override
    public void polymerNet$savePacketTime(Identifier identifier) {

    }

    @Override
    public ClientConnection polymerNet$getConnection() {
        return this.connection;
    }

    @Override
    public @Nullable DynamicRegistryManager polymer$getDynamicRegistryManager() {
        return null;
    }
}
