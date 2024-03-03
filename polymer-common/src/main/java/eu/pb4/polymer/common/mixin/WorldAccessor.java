package eu.pb4.polymer.common.mixin;

import net.minecraft.entity.damage.DamageSources;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Supplier;

@Mixin(World.class)
public interface WorldAccessor {
    @Mutable
    @Accessor("thread")
    void polymer$setThread(Thread thread);

    @Mutable
    @Accessor("debugWorld")
    void polymer$setDebugWorld(boolean debugWorld);

    @Mutable
    @Accessor("properties")
    void polymer$setProperties(MutableWorldProperties properties);

    @Mutable
    @Accessor("profiler")
    void polymer$setProfiler(Supplier<Profiler> profiler);

    @Mutable
    @Accessor("border")
    void polymer$setBorder(WorldBorder border);

    @Mutable
    @Accessor("biomeAccess")
    void polymer$setBiomeAccess(BiomeAccess biomeAccess);

    @Mutable
    @Accessor("registryKey")
    void polymer$setRegistryKey(RegistryKey<World> registryKey);

    @Mutable
    @Accessor("dimensionEntry")
    void polymer$setDimensionEntry(RegistryEntry<DimensionType> dimensionEntry);

    @Mutable
    @Accessor("random")
    void polymer$setRandom(Random random);

    @Mutable
    @Accessor("threadSafeRandom")
    void polymer$setAsyncRandom(Random random);

    @Mutable
    @Accessor("blockEntityTickers")
    void polymer$setBlockEntityTickers(List<BlockEntityTickInvoker> list);

    @Mutable
    @Accessor("pendingBlockEntityTickers")
    void polymer$setPendingBlockEntityTickers(List<BlockEntityTickInvoker> list);

    @Mutable
    @Accessor("damageSources")
    void polymer$setDamageSources(DamageSources sources);
}
