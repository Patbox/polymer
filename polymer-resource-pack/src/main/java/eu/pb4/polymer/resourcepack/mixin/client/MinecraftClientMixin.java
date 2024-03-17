package eu.pb4.polymer.resourcepack.mixin.client;

import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourceReloader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    @Final
    private TextureManager textureManager;

    @Shadow
    @Final
    private ReloadableResourceManagerImpl resourceManager;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;onFontOptionsChanged()V"))
    private void polymer$registerCustom(RunArgs args, CallbackInfo ci) {
        this.resourceManager.registerReloader(new PolymerResourceReloader(this.textureManager));
    }
}