package eu.pb4.polymer.core.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;

@Mixin(ChunkMap.TrackedEntity.class)
public interface TrackedEntityAccessor {
    @Accessor
    Set<ServerPlayerConnection> getSeenBy();

    @Accessor
    ServerEntity getServerEntity();
}
