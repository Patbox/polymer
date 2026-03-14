package eu.pb4.polymer.common.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.impl.CommonImplPacketKeys;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements PacketContextProvider {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void storeRegistries(MinecraftServer server, Connection connection, boolean transferred, CallbackInfo ci) {
        this.getPacketContext().set(CommonImplPacketKeys.HOLDER_LOOKUP, server.registryAccess());
    }
}
