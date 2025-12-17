package eu.pb4.polymer.virtualentity.mixin.accessors;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
    @Accessor("entityMap")
    Int2ObjectMap<ChunkMap.TrackedEntity> getEntityTrackers();

    @Accessor("serverViewDistance")
    int getWatchDistance();

    @Accessor
    Long2ObjectLinkedOpenHashMap<ChunkHolder> getVisibleChunkMap();
}
