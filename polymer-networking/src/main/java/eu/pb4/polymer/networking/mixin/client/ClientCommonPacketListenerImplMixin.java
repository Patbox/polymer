package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.PacketListenerImplExtension;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin implements PacketListenerImplExtension {
    @Shadow @Final protected Connection connection;

    @Shadow @Final protected Minecraft minecraft;

    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V", at = @At("HEAD"), cancellable = true)
    private void polymerNet$catchPackets(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (ClientPacketRegistry.handle(this.minecraft, (ClientCommonPacketListenerImpl) (Object) this, packet.payload())) {
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
    public Connection polymerNet$getConnection() {
        return this.connection;
    }

    @Override
    public @Nullable RegistryAccess polymer$getDynamicRegistryManager() {
        return null;
    }
}
