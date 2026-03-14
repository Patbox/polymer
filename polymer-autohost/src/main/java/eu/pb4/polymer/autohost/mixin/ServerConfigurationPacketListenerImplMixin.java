package eu.pb4.polymer.autohost.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.autohost.impl.AutoHostTask;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.config.ServerResourcePackConfigurationTask;
import net.minecraft.server.players.NameAndId;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Shadow @Final private Queue<ConfigurationTask> configurationTasks;

    @Shadow @Nullable private ConfigurationTask currentTask;

    @Shadow protected abstract void finishCurrentTask(ConfigurationTask.Type key);

    @Shadow protected abstract GameProfile playerProfile();

    @Inject(method = "addOptionalTasks", at = @At("TAIL"))
    private void polymerAutoHost$addTask(CallbackInfo ci) {
        if (AutoHost.config.enabled && !this.server.isSingleplayerOwner(new NameAndId(this.playerProfile()))) {
            var x = new ArrayList<MinecraftServer.ServerResourcePackInfo>();
            var ready = AutoHost.provider.isReady();
            if (ready) {
                x.addAll(AutoHost.provider.getProperties(this.connection));
            }
            x.addAll(AutoHost.GLOBAL_RESOURCE_PACKS);

            this.configurationTasks.add(new AutoHostTask(x, !ready, () -> AutoHost.provider.getProperties(this.connection), AutoHost.provider::isReady));
        }
    }

    @Inject(method = "handleResourcePackResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerCommonPacketListenerImpl;handleResourcePackResponse(Lnet/minecraft/network/protocol/common/ServerboundResourcePackPacket;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onStatus(ServerboundResourcePackPacket packet, CallbackInfo ci) {
        if (this.currentTask instanceof AutoHostTask task) {
            if (task.onStatus((ServerConfigurationPacketListenerImpl) (Object) this, packet.id(), packet.action())) {
                this.finishCurrentTask(AutoHostTask.KEY);
            }
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickTask(CallbackInfo ci) {
        if (this.currentTask instanceof AutoHostTask task) {
            task.tick(this::send);
        }
    }

    @WrapWithCondition(method = "handleResourcePackResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;finishCurrentTask(Lnet/minecraft/server/network/ConfigurationTask$Type;)V"))
    private boolean checkType(ServerConfigurationPacketListenerImpl instance, ConfigurationTask.Type key, @Local ServerboundResourcePackPacket packet) {
        return key != ServerResourcePackConfigurationTask.TYPE
                || (this.server.getServerResourcePack().isPresent() && this.server.getServerResourcePack().get().id().equals(packet.id()));
    }
}
