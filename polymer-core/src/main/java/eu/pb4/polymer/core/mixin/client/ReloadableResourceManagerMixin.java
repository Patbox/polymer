package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Environment(EnvType.CLIENT)
@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
    @Inject(method = "createReload", at = @At("RETURN"))
    private void polymer$onReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<PackResources> packs, CallbackInfoReturnable<ReloadInstance> cir) {
        var player = ClientUtils.getPlayer();
        if (player != null) {
            player.level().getServer().execute(() -> PolymerUtils.reloadWorld(player));
        }
    }
}
