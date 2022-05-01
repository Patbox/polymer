package eu.pb4.polymer.impl.other;

import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.mixin.other.DimensionTypeAccessor;
import eu.pb4.polymer.mixin.other.WorldAccessor;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.util.registry.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class FakeWorld extends World {
    public static final World INSTANCE;
    static final Scoreboard SCOREBOARD = new Scoreboard();
    static final DynamicRegistryManager REGISTRY_MANAGER = DynamicRegistryManager.createAndLoad();
    static final Biome BIOME = OverworldBiomeCreator.createTheVoid();
    static final RecipeManager RECIPE_MANAGER = new RecipeManager();
    static final ChunkManager CHUNK_MANAGER = new ChunkManager() {
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
            return null;
        }

        @Override
        public BlockView getWorld() {
            return INSTANCE;
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
        public <U extends Entity> void forEach(TypeFilter<Entity, U> filter, Consumer<U> action) {

        }

        @Override
        public void forEachIntersects(Box box, Consumer<Entity> action) {

        }

        @Override
        public <U extends Entity> void forEachIntersects(TypeFilter<Entity, U> filter, Box box, Consumer<U> action) {

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
        World world;

        try {
            try {
                world = (FakeWorld) UnsafeAccess.UNSAFE.allocateInstance(FakeWorld.class);
                var accessor = (WorldAccessor) world;
                accessor.polymer_setBiomeAccess(new BiomeAccess(world, 1l));
                accessor.polymer_setBorder(new WorldBorder());
                accessor.polymer_setDebugWorld(true);
                accessor.polymer_setProfiler(() -> new ProfilerSystem(() -> 0l, () -> 0, false));
                accessor.polymer_setProperties(new FakeWorldProperties());
                accessor.polymer_setRegistryKey(RegistryKey.of(Registry.WORLD_KEY, PolymerImplUtils.id("fake_world")));
                accessor.polymer_setThread(Thread.currentThread());
                accessor.polymer_setRandom(new Random());
                accessor.polymer_setBlockEntityTickers(new ArrayList<>());
                accessor.polymer_setPendingBlockEntityTickers(new ArrayList<>());

            } catch (Throwable e) {
                PolymerImpl.LOGGER.error("Creating fake world with unsafe failed... Time for plan B", e);
                world = new FakeWorld(
                        new FakeWorldProperties(),
                        RegistryKey.of(Registry.WORLD_KEY, PolymerImplUtils.id("fake_world")),
                        new RegistryEntry.Direct<>(DimensionTypeAccessor.polymer_getOverworld()),
                        () -> new ProfilerSystem(() -> 0l, () -> 0, false),
                        false,
                        true,
                        1
                );
            }
        } catch (Throwable e) {
            PolymerImpl.LOGGER.error("And it failed again... some mod is really angry at this stuff... setting it to null for now, hopefully it will pass enough", e);
            world = null;
        }


        INSTANCE = world;
    }

    protected FakeWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity player, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {

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

    @Nullable
    @Override
    public MapState getMapState(String id) {
        return null;
    }

    @Override
    public void putMapState(String id, MapState state) {

    }

    @Override
    public int getNextMapId() {
        return 0;
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
        return CHUNK_MANAGER;
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {

    }

    @Override
    public void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {

    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return REGISTRY_MANAGER;
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
        return BuiltinRegistries.BIOME.getEntry(BiomeKeys.THE_VOID).get();
    }


    static class FakeWorldProperties implements MutableWorldProperties {

        @Override
        public int getSpawnX() {
            return 0;
        }

        @Override
        public void setSpawnX(int spawnX) {

        }

        @Override
        public int getSpawnY() {
            return 0;
        }

        @Override
        public void setSpawnY(int spawnY) {

        }

        @Override
        public int getSpawnZ() {
            return 0;
        }

        @Override
        public void setSpawnZ(int spawnZ) {

        }

        @Override
        public float getSpawnAngle() {
            return 0;
        }

        @Override
        public void setSpawnAngle(float angle) {

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
            return new GameRules();
        }

        @Override
        public Difficulty getDifficulty() {
            return Difficulty.NORMAL;
        }

        @Override
        public boolean isDifficultyLocked() {
            return false;
        }
    }
}
