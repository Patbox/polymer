package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow public abstract List<ServerPlayer> getPlayers();

    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void polymerCore$invalidateItemGroups(CallbackInfo ci) {
        PolymerCreativeModeTabUtils.invalidateCache();
        for (var player : this.getPlayers()) {
            PolymerSyncUtils.synchronizeCreativeTabs(player.connection);
        }
    }
}
