package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocol;
import eu.pb4.polymer.core.impl.client.rendering.PolymerResourceReloader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
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
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    @Nullable
    public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow @Final private TextureManager textureManager;

    @Shadow @Final private ReloadableResourceManagerImpl resourceManager;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;initFont(Z)V"))
    private void polymer$registerCustom(RunArgs args, CallbackInfo ci) {
        this.resourceManager.registerReloader(new PolymerResourceReloader(this.textureManager));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void polymer$tick(CallbackInfo ci) {
        if (InternalClientRegistry.enabled) {
            InternalClientRegistry.tick();
        }
    }
    
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setWorld(Lnet/minecraft/client/world/ClientWorld;)V"))
    private void polymer$onDisconnect(CallbackInfo ci) {
        InternalClientRegistry.disable();
    }

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void polymer$pickBlock(CallbackInfo ci) {
        if (PolymerImpl.CHANGING_QOL_CLIENT && InternalClientRegistry.enabled && this.getNetworkHandler() != null && this.crosshairTarget != null) {
            switch (this.crosshairTarget.getType()) {
                case BLOCK -> {
                    var pos = ((BlockHitResult) this.crosshairTarget).getBlockPos();

                    if (InternalClientRegistry.getBlockAt(pos) != ClientPolymerBlock.NONE_STATE) {
                        PolymerClientProtocol.sendPickBlock(this.getNetworkHandler(), pos);
                        ci.cancel();
                    }
                }
                case ENTITY -> {
                    var entity = ((EntityHitResult) this.crosshairTarget).getEntity();

                    if (PolymerClientUtils.getEntityType(entity) != null) {
                        PolymerClientProtocol.sendPickEntity(this.getNetworkHandler(), entity.getId());
                        ci.cancel();
                    }
                }
            }
        }
    }
}