package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Environment(EnvType.CLIENT)
@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin {
    @Inject(method = "reload", at = @At("RETURN"))
    private void polymer$onReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs, CallbackInfoReturnable<ResourceReload> cir) {
        var server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            server.execute(() -> PolymerUtils.reloadWorld(ClientUtils.getPlayer()));
        }
    }
}
