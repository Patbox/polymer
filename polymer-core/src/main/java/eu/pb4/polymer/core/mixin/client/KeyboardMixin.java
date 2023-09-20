package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocol;
import eu.pb4.polymer.core.impl.networking.C2SPackets;
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
            InternalClientRegistry.delayAction(C2SPackets.CHANGE_TOOLTIP + "|pre", 1000, () -> {
                PolymerClientProtocol.sendTooltipContext(this.client.getNetworkHandler());
            });
        }
    }


    @Inject(method = "processF3", at = @At("TAIL"), cancellable = true)
    private void polymer_processF3(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_0 && CommonImpl.DEVELOPER_MODE) {
            PolymerImplUtils.dumpRegistry();
            this.debugLog("Dumped Polymer Client registry!");
            cir.setReturnValue(true);
        }
    }
}
