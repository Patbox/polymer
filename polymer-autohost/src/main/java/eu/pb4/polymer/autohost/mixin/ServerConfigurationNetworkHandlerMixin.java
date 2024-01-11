package eu.pb4.polymer.autohost.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.autohost.impl.AutoHostTask;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Queue;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigurationNetworkHandlerMixin extends ServerCommonNetworkHandler {
    public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Shadow @Final private Queue<ServerPlayerConfigurationTask> tasks;

    @Shadow @Nullable private ServerPlayerConfigurationTask currentTask;

    @Shadow protected abstract void onTaskFinished(ServerPlayerConfigurationTask.Key key);

    @Inject(method = "queueSendResourcePackTask", at = @At("TAIL"))
    private void polymerAutoHost$addTask(CallbackInfo ci) {
        if (AutoHost.config.enabled) {
            var x = new ArrayList<MinecraftServer.ServerResourcePackProperties>();
            x.addAll(AutoHost.provider.getProperties(this.connection));
            x.addAll(AutoHost.GLOBAL_RESOURCE_PACKS);

            this.tasks.add(new AutoHostTask(x));
        }
    }

    @Inject(method = "onResourcePackStatus", at = @At("TAIL"))
    private void onStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        if (this.currentTask instanceof AutoHostTask task && task.onStatus((ServerConfigurationNetworkHandler) (Object) this, packet.id(), packet.status())) {
            this.onTaskFinished(AutoHostTask.KEY);
        }
    }

    @WrapWithCondition(method = "onResourcePackStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationNetworkHandler;onTaskFinished(Lnet/minecraft/server/network/ServerPlayerConfigurationTask$Key;)V"))
    private boolean checkType(ServerConfigurationNetworkHandler instance, ServerPlayerConfigurationTask.Key key, @Local ResourcePackStatusC2SPacket packet) {
        return key != SendResourcePackTask.KEY
                || (this.server.getResourcePackProperties().isPresent() && this.server.getResourcePackProperties().get().id().equals(packet.id()));
    }
}
