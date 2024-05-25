package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ServerChunkLoadingManager.EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor
    Set<PlayerAssociatedNetworkHandler> getListeners();
}
