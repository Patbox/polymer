package eu.pb4.polymer.common.impl;


import eu.pb4.polymer.common.mixin.LevelAccessor;
import eu.pb4.polymer.common.mixin.ReferenceAccessor;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.core.*;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.chicken.ChickenSoundVariants;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.cow.CowSoundVariants;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.feline.CatSoundVariants;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.animal.pig.PigSoundVariants;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "unchecked"})
@ApiStatus.Internal
public final class FakeWorld extends Level implements LightChunk {
    public static final Level INSTANCE;

    public static final Level INSTANCE_UNSAFE;
    public static final Level INSTANCE_REGULAR;
    static final Scoreboard SCOREBOARD = new Scoreboard();

    static final RegistryAccess FALLBACK_REGISTRY_MANAGER = new RegistryAccess.Frozen() {
        private static final Map<ResourceKey<?>, Registry<?>> REGISTRIES = new HashMap<>();

        static {
            addRegistry(new FakeRegistry<>(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("polymer", "fake_damage"),
                    new DamageType("", DamageScaling.NEVER, 0)));
            addRegistry(new FakeRegistry<>(Registries.BANNER_PATTERN,
                    Identifier.fromNamespaceAndPath("polymer", "fake_pattern"),
                    new BannerPattern(Identifier.fromNamespaceAndPath("polymer", "fake_pattern"), "")));
            addRegistry(new FakeRegistry<>(Registries.PAINTING_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "painting"),
                    new PaintingVariant(1, 1, Identifier.fromNamespaceAndPath("polymer", "painting"), Optional.empty(), Optional.empty())));
            addRegistry(new FakeRegistry<>(Registries.WOLF_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "wolf"),
                    new WolfVariant(new WolfVariant.AssetInfo(
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf")),
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf")),
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf"))),
                            new WolfVariant.AssetInfo(
                                    new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf")),
                                    new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf")),
                                    new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf"))), SpawnPrioritySelectors.EMPTY)));

            addRegistry(new FakeRegistry<>(Registries.COW_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "cow"),
                    new CowVariant(
                            new ModelAndTexture<>(CowVariant.ModelType.NORMAL, new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf"))),
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf")),
                            SpawnPrioritySelectors.EMPTY)));

            addRegistry(new FakeRegistry<>(Registries.PIG_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "pig"),
                    new PigVariant(
                            new ModelAndTexture<>(PigVariant.ModelType.NORMAL, new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf"))),
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf")),
                            SpawnPrioritySelectors.EMPTY)));

            addRegistry(new FakeRegistry<>(Registries.CHICKEN_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "chicken"),
                    new ChickenVariant(
                            new ModelAndTexture<>(ChickenVariant.ModelType.NORMAL, new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf"))),
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "wolf")),
                            SpawnPrioritySelectors.EMPTY)));

            addRegistry(new FakeRegistry<>(Registries.CAT_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "cat"),
                    new CatVariant(
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "cat")),
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "cat")),
                            SpawnPrioritySelectors.EMPTY)));
            addRegistry(new FakeRegistry<>(Registries.FROG_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "frog"),
                    new FrogVariant(
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "frog")
                            ), SpawnPrioritySelectors.EMPTY)));
            addRegistry(new FakeRegistry<>(Registries.WOLF_SOUND_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "wolf"),
                    SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CLASSIC)));

            addRegistry(new FakeRegistry<>(Registries.CAT_SOUND_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "cat"),
                    SoundEvents.CAT_SOUNDS.get(CatSoundVariants.SoundSet.CLASSIC)));

            addRegistry(new FakeRegistry<>(Registries.CHICKEN_SOUND_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "chicken"),
                    SoundEvents.CHICKEN_SOUNDS.get(ChickenSoundVariants.SoundSet.CLASSIC)));

            addRegistry(new FakeRegistry<>(Registries.COW_SOUND_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "cow"),
                    SoundEvents.COW_SOUNDS.get(CowSoundVariants.SoundSet.CLASSIC)));

            addRegistry(new FakeRegistry<>(Registries.PIG_SOUND_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "pig"),
                    SoundEvents.PIG_SOUNDS.get(PigSoundVariants.SoundSet.CLASSIC)));

            addRegistry(new FakeRegistry<>(Registries.ZOMBIE_NAUTILUS_VARIANT,
                    Identifier.fromNamespaceAndPath("polymer", "zombie_noutilus_variant"),
                    new ZombieNautilusVariant(new ModelAndTexture<>(ZombieNautilusVariant.ModelType.NORMAL,
                            new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("polymer", "zombie_noutilus"))), SpawnPrioritySelectors.EMPTY)));

            addRegistry(new FakeRegistry<>(Registries.BIOME, Identifier.fromNamespaceAndPath("polymer", "fake_biome"),
                    new Biome.BiomeBuilder()
                            .temperature(0)
                            .downfall(0)
                            .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0).build())
                            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                            .generationSettings(BiomeGenerationSettings.EMPTY)
                            .build()));
        }

        public static void addRegistry(FakeRegistry<?> registry) {
            REGISTRIES.put(registry.key(), registry);
        }

        @Override
        public Optional<Registry> lookup(ResourceKey key) {
            var x = BuiltInRegistries.REGISTRY.getValue(key);
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
        public Stream<RegistryEntry<?>> registries() {
            return Stream.empty();
        }
    };
    static final RecipeManager RECIPE_MANAGER = new RecipeManager(FALLBACK_REGISTRY_MANAGER);
    private static final FeatureFlagSet FEATURES = FeatureFlags.REGISTRY.allFlags();
    private static final FuelValues FUEL_REGISTRY = new FuelValues.Builder(FALLBACK_REGISTRY_MANAGER, FeatureFlagSet.of()).build();
    private static final LevelEntityGetter<Entity> ENTITY_LOOKUP = new LevelEntityGetter<>() {
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
        public Iterable<Entity> getAll() {
            return () -> ObjectIterators.emptyIterator();
        }

        @Override
        public <U extends Entity> void get(EntityTypeTest<Entity, U> filter, AbortableIterationConsumer<U> consumer) {

        }

        @Override
        public void get(AABB box, Consumer<Entity> action) {

        }

        @Override
        public <U extends Entity> void get(EntityTypeTest<Entity, U> filter, AABB box, AbortableIterationConsumer<U> consumer) {

        }

    };
    private static final LevelTickAccess<?> FAKE_SCHEDULER = new LevelTickAccess<Object>() {
        @Override
        public boolean willTickThisTick(BlockPos pos, Object type) {
            return false;
        }

        @Override
        public void schedule(ScheduledTick<Object> orderedTick) {

        }

        @Override
        public boolean hasScheduledTick(BlockPos pos, Object type) {
            return false;
        }

        @Override
        public int count() {
            return 0;
        }
    };

    static {
        Level worldUnsafe, worldDefault;

        var dimType = Holder.Reference.createIntrusive(new HolderOwner<>() {
                                                       },
                new DimensionType(true, false, false, false,1.0D,
                        -64, 256, 256, BlockTags.INFINIBURN_OVERWORLD, 1,
                        new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                        DimensionType.Skybox.NONE, CardinalLighting.Type.DEFAULT, EnvironmentAttributeMap.builder().build(), HolderSet.empty(),
                        Optional.empty()));
        ((ReferenceAccessor) dimType).callBindKey(ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.parse("overworld")));
        try {
            worldUnsafe = (FakeWorld) UnsafeAccess.UNSAFE.allocateInstance(FakeWorld.class);
            var accessor = (LevelAccessor) worldUnsafe;
            accessor.polymer$setBiomeAccess(new BiomeManager(worldUnsafe, 1L));
            accessor.polymer$setDebugWorld(true);
            accessor.polymer$setProperties(new FakeWorldProperties());
            accessor.polymer$setRegistryKey(ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("polymer", "fake_world")));
            //accessor.polymer$setDimensionKey(DimensionTypes.OVERWORLD);
            accessor.polymer$setDimensionEntry(dimType);
            accessor.polymer$setThread(Thread.currentThread());
            accessor.polymer$setRandom(RandomSource.create());
            accessor.polymer$setAsyncRandom(RandomSource.createThreadSafe());
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
                    ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("polymer", "fake_world")),
                    dimType,
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

    final ChunkSource chunkManager = new ChunkSource() {
        private LevelLightEngine lightingProvider = null;

        @Nullable
        @Override
        public ChunkAccess getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
            return null;
        }

        @Override
        public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {

        }

        @Override
        public String gatherStats() {
            return "Potato";
        }

        @Override
        public int getLoadedChunksCount() {
            return 0;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            if (this.lightingProvider == null) {
                this.lightingProvider = new LevelLightEngine(new LightChunkGetter() {
                    @Nullable
                    @Override
                    public LightChunk getChunkForLighting(int chunkX, int chunkZ) {
                        return FakeWorld.this;
                    }

                    @Override
                    public BlockGetter getLevel() {
                        return FakeWorld.this;
                    }
                }, false, false);
            }

            return this.lightingProvider;
        }

        @Override
        public BlockGetter getLevel() {
            return FakeWorld.this;
        }
    };
    private final TickRateManager tickManager = new TickRateManager();
    private final WorldBorder worldBorder = new WorldBorder();
    private final ClockManager clockManager = new ClockManager() {
        @Override
        public long getTotalTicks(Holder<WorldClock> definition) {
            return 0;
        }
    };

    private FakeWorld(WritableLevelData properties, ResourceKey<Level> registryRef, Holder<DimensionType> dimensionType, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, FALLBACK_REGISTRY_MANAGER, dimensionType, isClient, debugWorld, seed, 0);
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public void playSeededSound(@Nullable Entity source, double x, double y, double z, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSeededSound(@Nullable Entity source, Entity entity, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {

    }

    @Override
    public void explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator behavior, double x, double y, double z, float power, boolean createFire, ExplosionInteraction explosionSourceType, ParticleOptions smallParticle, ParticleOptions largeParticle, WeightedList<ExplosionParticleInfo> blockParticles, Holder<SoundEvent> soundEvent) {

    }

    @Override
    public String gatherChunkSourceStats() {
        return "FakeWorld!";
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return null;
    }

    @Override
    public void setRespawnData(LevelData.RespawnData spawnPoint) {

    }

    @Nullable
    @Override
    public Entity getEntity(int id) {
        return null;
    }

    @Override
    public Collection<EnderDragonPart> dragonParts() {
        return List.of();
    }


    @Override
    public TickRateManager tickRateManager() {
        return this.tickManager;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(MapId id) {
        return null;
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        return this.registryAccess().getOrThrow(Biomes.THE_VOID);
    }

    @Override
    public @UnknownNullability Holder<Biome> getBiomeFabric(BlockPos pos) {
        return getBiome(pos);
    }

    @Override
    public void destroyBlockProgress(int entityId, BlockPos pos, int progress) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return SCOREBOARD;
    }

    @Override
    public RecipeAccess recipeAccess() {
        return RECIPE_MANAGER;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return ENTITY_LOOKUP;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return (LevelTickAccess<Block>) FAKE_SCHEDULER;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return (LevelTickAccess<Fluid>) FAKE_SCHEDULER;
    }

    @Override
    public ChunkSource getChunkSource() {
        return chunkManager;
    }

    @Override
    public void levelEvent(@Nullable Entity source, int eventId, BlockPos pos, int data) {

    }

    @Override
    public void gameEvent(Holder<GameEvent> event, Vec3 emitterPos, GameEvent.Context emitter) {

    }

    @Override
    public RegistryAccess registryAccess() {
        return FALLBACK_REGISTRY_MANAGER;
    }

    @Override
    public ClockManager clockManager() {
        return this.clockManager;
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes() {
        return EnvironmentAttributeSystem.builder().build();
    }

    @Override
    public PotionBrewing potionBrewing() {
        return null;
    }


    @Override
    public FuelValues fuelValues() {
        return FUEL_REGISTRY;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FEATURES;
    }

    @Override
    public List<? extends Player> players() {
        return Collections.emptyList();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        return getBiome(null);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public void findBlockLightSources(BiConsumer<BlockPos, BlockState> callback) {

    }

    @Override
    public ChunkSkyLightSources getSkyLightSources() {
        return null;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }


    static class FakeWorldProperties implements WritableLevelData {


        @Override
        public RespawnData getRespawnData() {
            return null;
        }

        @Override
        public long getGameTime() {
            return 0;
        }

        @Override
        public boolean isHardcore() {
            return false;
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
        public void setSpawn(RespawnData spawnPoint) {

        }
    }
}
