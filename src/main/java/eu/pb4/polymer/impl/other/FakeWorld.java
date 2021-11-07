package eu.pb4.polymer.impl.other;

import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.mixin.other.DimensionTypeAccessor;
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
import net.minecraft.tag.TagManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@ApiStatus.Internal
class FakeWorld extends World {
    static public World INSTANCE;

    private static final DynamicRegistryManager REGISTRY_MANAGER = new DynamicRegistryManager() {
        @Override
        public <E> Optional<MutableRegistry<E>> getOptionalMutable(RegistryKey<? extends Registry<? extends E>> key) {
            return Optional.empty();
        }
    };

    private static final Biome FAKE_BIOME = DefaultBiomeCreator.createTheVoid();

    private static final ChunkManager CHUNK_MANAGER = new ChunkManager() {
        LightingProvider provider = new LightingProvider(this, false, false);
        @Nullable
        @Override
        public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
            return null;
        }

        @Override
        public void tick(BooleanSupplier booleanSupplier) {

        }

        @Override
        public String getDebugString() {
            return "FakeChunkManager!";
        }

        @Override
        public int getLoadedChunkCount() {
            return 0;
        }

        @Override
        public LightingProvider getLightingProvider() {
            return provider;
        }

        @Override
        public BlockView getWorld() {
            return INSTANCE;
        }
    };
    static private final Scoreboard SCOREBOARD = new Scoreboard();

    static {
        try {
            INSTANCE = new FakeWorld(new FakeWorldProperties(), null, DimensionTypeAccessor.polymer_getOverworld(), null, false, true, 1);
        } catch (Exception e1) {
            PolymerMod.LOGGER.error("Couldn't initiate fake world! See logs below!");
            throw e1;
        }
    }

    protected FakeWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
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
        return null;
    }

    @Override
    public TagManager getTagManager() {
        return TagManager.EMPTY;
    }

    @Override
    protected EntityLookup<Entity> getEntityLookup() {
        return null;
    }

    @Override
    public TickScheduler<Block> getBlockTickScheduler() {
        return null;
    }

    @Override
    public TickScheduler<Fluid> getFluidTickScheduler() {
        return null;
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
    public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return FAKE_BIOME;
    }


    static class FakeWorldProperties implements MutableWorldProperties {

        @Override
        public void setSpawnX(int spawnX) {

        }

        @Override
        public void setSpawnY(int spawnY) {

        }

        @Override
        public void setSpawnZ(int spawnZ) {

        }

        @Override
        public void setSpawnAngle(float angle) {

        }

        @Override
        public int getSpawnX() {
            return 0;
        }

        @Override
        public int getSpawnY() {
            return 0;
        }

        @Override
        public int getSpawnZ() {
            return 0;
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
