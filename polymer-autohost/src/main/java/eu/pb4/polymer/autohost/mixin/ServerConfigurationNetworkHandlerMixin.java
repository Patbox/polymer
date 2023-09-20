package eu.pb4.polymer.autohost.mixin;

import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigurationNetworkHandlerMixin extends ServerCommonNetworkHandler {
    public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Shadow protected abstract void queueSendResourcePackTask();

    @Shadow @Final private Queue<ServerPlayerConfigurationTask> tasks;

    @Inject(method = "queueSendResourcePackTask", at = @At("HEAD"), cancellable = true)
    private void polymerAutoHost$addTask(CallbackInfo ci) {
        if (AutoHost.config.enabled && !PolymerCommonUtils.hasResourcePack(this.connection)) {
            this.tasks.add(new SendResourcePackTask(AutoHost.provider.getProperties()));
            ci.cancel();
        }
    }
}
