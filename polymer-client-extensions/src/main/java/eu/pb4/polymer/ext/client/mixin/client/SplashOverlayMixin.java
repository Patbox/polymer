package eu.pb4.polymer.ext.client.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import eu.pb4.polymer.ext.client.api.PolymerClientExtensions;
import eu.pb4.polymer.ext.client.impl.client.CERegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.apache.http.util.Args;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
    @Shadow @Final private MinecraftClient client;
    @Unique
    private float pce_theLetterH = 0;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
    private Identifier pce_replaceLogo(Identifier vanilla) {
        if (CERegistry.customReloadLogo) {
            return switch (CERegistry.customReloadMode) {
                case ICON -> CERegistry.RELOAD_LOGO_IDENTIFIER;
                case NONE -> CERegistry.EMPTY_TEXTURE;
                default -> vanilla;
            };
        }
        return vanilla;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"), index = 3)
    private float pce_catchH(float h) {
        this.pce_theLetterH = h;
        return h;
    }
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"))
    private void pce_drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (CERegistry.customReloadLogo && CERegistry.customReloadMode == PolymerClientExtensions.ReloadLogoOverride.FULL_SCREEN) {
            int width = this.client.getWindow().getScaledWidth();
            int height = this.client.getWindow().getScaledHeight();
            RenderSystem.setShaderTexture(0, CERegistry.RELOAD_LOGO_IDENTIFIER);
            RenderSystem.enableBlend();
            RenderSystem.blendEquation(32774);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.pce_theLetterH);
            DrawableHelper.drawTexture(matrices, 0, 0, 0, 0, 0, width, height, width, height);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFunc(II)V"), index = 0)
    private int pce_replaceBlend1(int vanilla) {
        if (CERegistry.customReloadLogo && CERegistry.customReloadMode != PolymerClientExtensions.ReloadLogoOverride.DEFAULT) {
            return GlStateManager.SrcFactor.SRC_ALPHA.value;
        }
        return vanilla;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFunc(II)V"), index = 1)
    private int pce_replaceBlend2(int vanilla) {
        if (CERegistry.customReloadLogo && CERegistry.customReloadMode != PolymerClientExtensions.ReloadLogoOverride.DEFAULT) {
            return GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value;
        }
        return vanilla;
    }

    @Inject(method = "method_35733", at = @At("HEAD"), cancellable = true)
    private static void pce_replaceColor(CallbackInfoReturnable<Integer> cir) {
        if (CERegistry.customReloadLogo) {
            cir.setReturnValue(MinecraftClient.getInstance().options.monochromeLogo ? CERegistry.customReloadColorDark : CERegistry.customReloadColor);
        }
    }

    @ModifyArgs(method = "renderProgressBar", at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/hud/BackgroundHelper$ColorMixer;getArgb(IIII)I"))
    private void pce_replaceBarColor(org.spongepowered.asm.mixin.injection.invoke.arg.Args args) {
        if (CERegistry.customReloadLogo) {
            var color = (MinecraftClient.getInstance().options.monochromeLogo ? CERegistry.customReloadColorBarDark : CERegistry.customReloadColorBar);

            args.set(1, (color >> 16) & 0xFF);
            args.set(2, (color >> 8) & 0xFF);
            args.set(3, color & 0xFF);
        }
    }

}
