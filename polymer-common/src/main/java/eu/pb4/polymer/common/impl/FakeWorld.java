package eu.pb4.polymer.common.impl;


import eu.pb4.polymer.common.mixin.ReferenceAccessor;
import eu.pb4.polymer.common.mixin.WorldAccessor;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.map.MapState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.ChunkSkyLight;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "unchecked"})
@ApiStatus.Internal
public final class FakeWorld extends World implements LightSourceView {
    public static final World INSTANCE;

    public static final World INSTANCE_UNSAFE;
    public static final World INSTANCE_REGULAR;
    static final Scoreboard SCOREBOARD = new Scoreboard();

    static final DynamicRegistryManager FALLBACK_REGISTRY_MANAGER = new DynamicRegistryManager.Immutable() {
        private static final Map<RegistryKey<?>, Registry<?>> REGISTRIES = new HashMap<>();
        @Override
        public Optional<Registry> getOptional(RegistryKey key) {
            var x = Registries.REGISTRIES.get(key);
            if (x != null) {
                return Optional.of(x);
            }

            var reg = REGISTRIES.get(key);

            if (reg != null) {
                return Optional.of(reg);
            }

            return Optional.empty();
        }

        @Override
        public Stream<Entry<?>> streamAllRegistries() {
            return Stream.empty();
        }

        public static void addRegistry(FakeRegistry<?> registry) {
            REGISTRIES.put(registry.getKey(), registry);
        }

        static {
            addRegistry(new FakeRegistry<>(RegistryKeys.DAMAGE_TYPE, Identifier.of("polymer","fake_damage"),
                    new DamageType("", DamageScaling.NEVER, 0)));
            addRegistry(new FakeRegistry<>(RegistryKeys.BANNER_PATTERN,
                    Identifier.of("polymer","fake_pattern"),
                    new BannerPattern(Identifier.of("polymer","fake_pattern"), "")));
            addRegistry(new FakeRegistry<>(RegistryKeys.PAINTING_VARIANT,
                    Identifier.of("polymer","painting"),
                    new PaintingVariant(1, 1, Identifier.of("polymer","painting"))));
            addRegistry(new FakeRegistry<>(RegistryKeys.WOLF_VARIANT,
                    Identifier.of("polymer","wolf"),
                    new WolfVariant(Identifier.of("polymer","wolf"), Identifier.of("polymer","wolf"),Identifier.of("polymer","wolf"), RegistryEntryList.empty())));
        }
    };
    static final RecipeManager RECIPE_MANAGER = new RecipeManager(FALLBACK_REGISTRY_MANAGER);
    private static final FeatureSet FEATURES = FeatureFlags.FEATURE_MANAGER.getFeatureSet();
    private static final FuelRegistry FUEL_REGISTRY = new FuelRegistry.Builder(FALLBACK_REGISTRY_MANAGER, FeatureSet.empty()).build();
    final ChunkManager chunkManager = new ChunkManager() {
        private LightingProvider lightingProvider = null;

        @Nullable
        @Override
        public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
            return null;
        }

        @Override
        public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {

        }

        @Override
        public String getDebugString() {
            return "Potato";
        }

        @Override
        public int getLoadedChunkCount() {
            return 0;
        }

        @Override
        public LightingProvider getLightingProvider() {
            if (this.lightingProvider == null) {
                this.lightingProvider = new LightingProvider(new ChunkProvider() {
                    @Nullable
                    @Override
                    public LightSourceView getChunk(int chunkX, int chunkZ) {
                        return FakeWorld.this;
                    }

                    @Override
                    public BlockView getWorld() {
                        return FakeWorld.this;
                    }
                }, false, false);
            }

            return this.lightingProvider;
        }

        @Override
        public BlockView getWorld() {
            return FakeWorld.this;
        }
    };
    private static final EntityLookup<Entity> ENTITY_LOOKUP = new EntityLookup<>() {
        @Nullable
        @Override
        public Entity get(int id) {
            return null;
        }

        @Nullable
        @Override
        public Entity get(UUID uuid) {
            return null;
        }

        @Override
        public Iterable<Entity> iterate() {
            return () -> ObjectIterators.emptyIterator();
        }

        @Override
        public <U extends Entity> void forEach(TypeFilter<Entity, U> filter, LazyIterationConsumer<U> consumer) {

        }

        @Override
        public void forEachIntersects(Box box, Consumer<Entity> action) {

        }

        @Override
        public <U extends Entity> void forEachIntersects(TypeFilter<Entity, U> filter, Box box, LazyIterationConsumer<U> consumer) {

        }

    };
    private static final QueryableTickScheduler<?> FAKE_SCHEDULER = new QueryableTickScheduler<Object>() {
        @Override
        public boolean isTicking(BlockPos pos, Object type) {
            return false;
        }

        @Override
        public void scheduleTick(OrderedTick<Object> orderedTick) {

        }

        @Override
        public boolean isQueued(BlockPos pos, Object type) {
            return false;
        }

        @Override
        public int getTickCount() {
            return 0;
        }
    };

    static {
        World worldUnsafe, worldDefault;

        var dimType = RegistryEntry.Reference.intrusive(new RegistryEntryOwner<>() {}, new DimensionType(OptionalLong.empty(), true, false, false, true, 1.0D, true, false, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, DimensionTypes.OVERWORLD_ID, 0.0F, new DimensionType.MonsterSettings(false, true, UniformIntProvider.create(0, 7), 0)));
        ((ReferenceAccessor) dimType).callSetRegistryKey(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of("overworld")));
        try {
            worldUnsafe = (FakeWorld) UnsafeAccess.UNSAFE.allocateInstance(FakeWorld.class);
            var accessor = (WorldAccessor) worldUnsafe;
            accessor.polymer$setBiomeAccess(new BiomeAccess(worldUnsafe, 1l));
            accessor.polymer$setBorder(new WorldBorder());
            accessor.polymer$setDebugWorld(true);
            accessor.polymer$setProfiler(() -> new ProfilerSystem(() -> 0l, () -> 0, false));
            accessor.polymer$setProperties(new FakeWorldProperties());
            accessor.polymer$setRegistryKey(RegistryKey.of(RegistryKeys.WORLD, Identifier.of("polymer","fake_world")));
            //accessor.polymer$setDimensionKey(DimensionTypes.OVERWORLD);
            accessor.polymer$setDimensionEntry(dimType);
            accessor.polymer$setThread(Thread.currentThread());
            accessor.polymer$setRandom(Random.create());
            accessor.polymer$setAsyncRandom(Random.createThreadSafe());
            accessor.polymer$setBlockEntityTickers(new ArrayList<>());
            accessor.polymer$setPendingBlockEntityTickers(new ArrayList<>());
            try {
                accessor.polymer$setDamageSources(new DamageSources(FALLBACK_REGISTRY_MANAGER));
            } catch (Throwable e) {

            }

        } catch (Throwable e) {
            CommonImpl.LOGGER.error("Creating fake world with unsafe failed...", e);
            worldUnsafe = null;
        }

        try {
            worldDefault = new FakeWorld(
                    new FakeWorldProperties(),
                    RegistryKey.of(RegistryKeys.WORLD, Identifier.of("polymer", "fake_world")),
                    dimType,
                    () -> new ProfilerSystem(() -> 0l, () -> 0, false),
                    false,
                    true,
                    1
            );
        } catch (Throwable e) {
            CommonImpl.LOGGER.error("Creating fake world in regular way failed...", e);
            worldDefault = null;
        }


        INSTANCE_UNSAFE = worldUnsafe;
        INSTANCE_REGULAR = worldDefault;

        INSTANCE = worldUnsafe != null ? worldUnsafe : worldDefault;
    }

    private TickManager tickManager = new TickManager();

    protected FakeWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, FALLBACK_REGISTRY_MANAGER, dimensionType, profiler, isClient, debugWorld, seed, 0);
    }

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> registryEntry, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> registryEntry, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity player, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType, ParticleEffect particle, ParticleEffect emitterParticle, RegistryEntry<SoundEvent> soundEvent) {

    }

    @Override
    public String asString() {
        return "FakeWorld!";
    }

    @Nullable
    @Override
    public Entity getEntityById(int id) {
        return null;
    }

    @Override
    public TickManager getTickManager() {
        return this.tickManager;
    }

    @Nullable
    @Override
    public MapState getMapState(MapIdComponent id) {
        return null;
    }

    @Override
    public void putMapState(MapIdComponent id, MapState state) {

    }

    @Override
    public MapIdComponent increaseAndGetMapId() {
        return null;
    }

    @Override
    public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return SCOREBOARD;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return RECIPE_MANAGER;
    }

    @Override
    protected EntityLookup<Entity> getEntityLookup() {
        return ENTITY_LOOKUP;
    }

    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler() {
        return (QueryableTickScheduler<Block>) FAKE_SCHEDULER;
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
        return (QueryableTickScheduler<Fluid>) FAKE_SCHEDULER;
    }

    @Override
    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {

    }

    @Override
    public void emitGameEvent(RegistryEntry<GameEvent> event, Vec3d emitterPos, GameEvent.Emitter emitter) {

    }
    @Override
    public DynamicRegistryManager getRegistryManager() {
        return FALLBACK_REGISTRY_MANAGER;
    }

    @Override
    public BrewingRecipeRegistry getBrewingRecipeRegistry() {
        return null;
    }


    @Override
    public FuelRegistry getFuelRegistry() {
        return FUEL_REGISTRY;
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return FEATURES;
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 0;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return null;//BuiltinRegistries.BIOME.getEntry(BiomeKeys.THE_VOID).get();
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public void forEachLightSource(BiConsumer<BlockPos, BlockState> callback) {

    }

    @Override
    public ChunkSkyLight getChunkSkyLight() {
        return null;
    }


    static class FakeWorldProperties implements MutableWorldProperties {
        @Override
        public BlockPos getSpawnPos() {
            return BlockPos.ORIGIN;
        }

        @Override
        public float getSpawnAngle() {
            return 0;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public long getTimeOfDay() {
            return 0;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return false;
        }

        @Override
        public void setRaining(boolean raining) {

        }

        @Override
        public boolean isHardcore() {
            return false;
        }

        @Override
        public GameRules getGameRules() {
            return new GameRules(FeatureSet.empty());
        }

        @Override
        public Difficulty getDifficulty() {
            return Difficulty.NORMAL;
        }

        @Override
        public boolean isDifficultyLocked() {
            return false;
        }

        @Override
        public void setSpawnPos(BlockPos pos, float angle) {

        }
    }
}
