package eu.pb4.polymer.mixin.other;

import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
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
    void polymer_setThread(Thread thread);

    @Mutable
    @Accessor("debugWorld")
    void polymer_setDebugWorld(boolean debugWorld);

    @Mutable
    @Accessor("properties")
    void polymer_setProperties(MutableWorldProperties properties);

    @Mutable
    @Accessor("profiler")
    void polymer_setProfiler(Supplier<Profiler> profiler);

    @Mutable
    @Accessor("border")
    void polymer_setBorder(WorldBorder border);

    @Mutable
    @Accessor("biomeAccess")
    void polymer_setBiomeAccess(BiomeAccess biomeAccess);

    @Mutable
    @Accessor("registryKey")
    void polymer_setRegistryKey(RegistryKey<World> registryKey);

    @Mutable
    @Accessor("dimension")
    void polymer_setDimensionKey(RegistryKey<DimensionType> dimension);

    @Mutable
    @Accessor("dimensionEntry")
    void polymer_setDimensionEntry(RegistryEntry<DimensionType> dimensionEntry);

    @Mutable
    @Accessor("random")
    void polymer_setRandom(Random random);

    @Mutable
    @Accessor("threadSafeRandom")
    void polymer_setAsyncRandom(Random random);

    @Mutable
    @Accessor("blockEntityTickers")
    void polymer_setBlockEntityTickers(List<BlockEntityTickInvoker> list);

    @Mutable
    @Accessor("pendingBlockEntityTickers")
    void polymer_setPendingBlockEntityTickers(List<BlockEntityTickInvoker> list);
}
