package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.ClientDebugFlags;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocol;
import eu.pb4.polymer.core.impl.networking.C2SPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract void debugFeedbackComponent(Component message);

    @Inject(method = "debugFeedback(Ljava/lang/String;)V", at = @At("HEAD"))
    private void polymer_catchChange(String key, CallbackInfo ci) {
        if (key.startsWith("debug.advanced_tooltips")) {
            InternalClientRegistry.delayAction(C2SPackets.CHANGE_TOOLTIP + "|pre", 1000, () -> {
                PolymerClientProtocol.sendTooltipContext(this.minecraft.getConnection());
            });
        }
    }


    @Inject(method = "handleDebugKeys", at = @At("TAIL"), cancellable = true)
    private void polymer_processF3(KeyEvent keyInput, CallbackInfoReturnable<Boolean> cir) {
        if (!CommonImpl.DEVELOPER_MODE) {
            return;
        }

        var key = keyInput.key();

        // Todo
        /*if (key == GLFW.GLFW_KEY_0) {
            PolymerImplUtils.dumpRegistry();
            this.debugFeedbackComponent(Component.literal("Dumped Polymer Client registry!"));
            cir.setReturnValue(true);
        } else if (key == GLFW.GLFW_KEY_LEFT_BRACKET) {
            ClientDebugFlags.customItemModels = !ClientDebugFlags.customItemModels;
            this.debugFeedbackComponent(Component.literal("Component item models: " + ClientDebugFlags.customItemModels));
            cir.setReturnValue(true);
        } else if (key == GLFW.GLFW_KEY_RIGHT_BRACKET) {
            ClientDebugFlags.customFonts = !ClientDebugFlags.customFonts;
            this.debugFeedbackComponent(Component.literal("Custom fonts: " + ClientDebugFlags.customFonts));
            cir.setReturnValue(true);
        }*/
    }
}
