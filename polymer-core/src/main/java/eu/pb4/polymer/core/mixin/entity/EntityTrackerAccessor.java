package eu.pb4.polymer.core.mixin.entity;

import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ServerChunkLoadingManager.EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor
    Set<PlayerAssociatedNetworkHandler> getListeners();

    @Accessor
    EntityTrackerEntry getEntry();
}
