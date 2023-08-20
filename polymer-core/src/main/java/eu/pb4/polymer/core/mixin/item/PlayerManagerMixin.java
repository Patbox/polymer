package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow public abstract List<ServerPlayerEntity> getPlayerList();

    @Inject(method = "onDataPacksReloaded", at = @At("HEAD"))
    private void polymerCore$invalidateItemGroups(CallbackInfo ci) {
        PolymerItemGroupUtils.invalidateItemGroupCache();
        for (var player : this.getPlayerList()) {
            PolymerSyncUtils.synchronizeCreativeTabs(player.networkHandler);
        }
    }
}
