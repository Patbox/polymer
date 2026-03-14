package eu.pb4.polymer.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(Level.class)
public interface LevelAccessor {
    @Mutable
    @Accessor("thread")
    void polymer$setThread(Thread thread);

    @Mutable
    @Accessor("isDebug")
    void polymer$setDebugWorld(boolean debugWorld);

    @Mutable
    @Accessor("levelData")
    void polymer$setProperties(WritableLevelData properties);

    @Mutable
    @Accessor("biomeManager")
    void polymer$setBiomeAccess(BiomeManager biomeAccess);

    @Mutable
    @Accessor("dimension")
    void polymer$setRegistryKey(ResourceKey<Level> registryKey);

    @Mutable
    @Accessor("dimensionTypeRegistration")
    void polymer$setDimensionEntry(Holder<DimensionType> dimensionEntry);

    @Mutable
    @Accessor("random")
    void polymer$setRandom(RandomSource random);

    @Mutable
    @Accessor("random")
    void polymer$setAsyncRandom(RandomSource random);

    @Mutable
    @Accessor("blockEntityTickers")
    void polymer$setBlockEntityTickers(List<TickingBlockEntity> list);

    @Mutable
    @Accessor("pendingBlockEntityTickers")
    void polymer$setPendingBlockEntityTickers(List<TickingBlockEntity> list);

    @Mutable
    @Accessor("damageSources")
    void polymer$setDamageSources(DamageSources sources);
}
