package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.repository.KnownPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @WrapOperation(method = "handleSelectKnownPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/config/SynchronizeRegistriesTask;handleResponse(Ljava/util/List;Ljava/util/function/Consumer;)V"))
    private void wrapWithContext(SynchronizeRegistriesTask instance, List<KnownPack> clientKnownPacks, Consumer<Packet<?>> sender, Operation<Void> original) {
        PacketContext.runWithContext(this, () -> {
            original.call(instance, clientKnownPacks, sender);
        });
    }
}
