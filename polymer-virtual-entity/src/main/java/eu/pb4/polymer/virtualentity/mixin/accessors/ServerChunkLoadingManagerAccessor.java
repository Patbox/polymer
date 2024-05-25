package eu.pb4.polymer.virtualentity.mixin.accessors;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkLoadingManager.class)
public interface ServerChunkLoadingManagerAccessor {
    @Accessor("entityTrackers")
    Int2ObjectMap<ServerChunkLoadingManager.EntityTracker> getEntityTrackers();

    @Accessor("watchDistance")
    int getWatchDistance();
}
