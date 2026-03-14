package eu.pb4.polymer.common.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.impl.CommonImplPacketKeys;
import eu.pb4.polymer.common.impl.CommonPacketListenerImplExt;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigurationPacketListenerImplMixin implements PacketContextProvider {
    @Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    private void storeRegistries(ClientboundFinishConfigurationPacket packet, CallbackInfo ci, @Local RegistryAccess.Frozen registryAccess) {
        this.getPacketContext().set(CommonImplPacketKeys.HOLDER_LOOKUP, registryAccess);
    }
}
