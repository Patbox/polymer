package eu.pb4.polymer.autohost.mixin;

import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.autohost.impl.AutoHostConfig;
import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Unique
    private boolean started;
    @Inject(method = "runServer", at = @At("HEAD"))
    private void polymer_autohost_initEarly(CallbackInfo ci) {
        try {
            var x = CommonImpl.loadConfig("auto-host", AutoHostConfig.class);
            if (x.loadEarly) {
                AutoHost.init((MinecraftServer) (Object) this);
                this.started = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;"))
    private void polymer_autohost_init(CallbackInfo ci) {
        if (!this.started) {
            AutoHost.init((MinecraftServer) (Object) this);
            this.started = true;
        }
    }

    @Inject(method = "shutdown", at = @At("TAIL"))
    private void polymer_autohost_end(CallbackInfo ci) {
        AutoHost.end((MinecraftServer) (Object) this);
    }
}
