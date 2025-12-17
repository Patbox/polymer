package eu.pb4.polymer.virtualentity.mixin.block;

import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelChunk.class)
public interface WorldChunkAccessor {
    @Accessor
    boolean isLoaded();
}
