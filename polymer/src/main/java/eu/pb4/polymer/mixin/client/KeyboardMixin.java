package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.networking.PolymerClientProtocol;
import eu.pb4.polymer.impl.networking.ClientPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void debugLog(String key, Object... args);

    @Inject(method = "debugLog(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"))
    private void polymer_catchChange(String key, Object[] args, CallbackInfo ci) {
        if (key.startsWith("debug.advanced_tooltips")) {
            InternalClientRegistry.delayAction(ClientPackets.CHANGE_TOOLTIP + "|pre", 1000, () -> {
                PolymerClientProtocol.sendTooltipContext(this.client.getNetworkHandler());
            });
        }
    }


    @Inject(method = "processF3", at = @At("TAIL"), cancellable = true)
    private void polymer_processF3(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_KP_4 && PolymerImpl.DEVELOPER_MODE) {
            PolymerImplUtils.dumpRegistry();
            this.debugLog("Dumped Polymer Client registry!");
            cir.setReturnValue(true);
        }
    }
}
