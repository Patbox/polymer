package eu.pb4.polymer.core.impl.other.world;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.mixin.block.PalettedContainerAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.map.MapState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public abstract class VirtualWorld extends World {
    protected final ServerWorld world;
    protected final Random random = Random.create();
    protected final World fakeWorld;
    protected final Long2ObjectOpenHashMap<PalettedContainer<BlockState>> sections = new Long2ObjectOpenHashMap<>();
    protected final Long2ObjectOpenHashMap<PalettedContainer<BlockState>> realSections = new Long2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenCustomHashMap<BlockState, BlockState> stateMap = new Object2ObjectOpenCustomHashMap<>(Util.identityHashStrategy());

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
            return ObjectIterators::emptyIterator;
        }

        @Override
        public <U extends Entity> void forEach(TypeFilter<Entity, U> filter, LazyIterationConsumer<U> consumer) {

        }

        @Override
        public void forEachIntersects(Box box, java.util.function.Consumer<Entity> action) {

        }

        @Override
        public <U extends Entity> void forEachIntersects(TypeFilter<Entity, U> filter, Box box, LazyIterationConsumer<U> consumer) {

        }
    };

    public VirtualWorld(ServerWorld world) {
        super((MutableWorldProperties) world.getLevelProperties(),
                world.getRegistryKey(),
                world.getRegistryManager(),
                world.getDimensionEntry(),
                world.getProfilerSupplier(),
                false,
                world.isDebugWorld(),
                0,
               world.getServer().getMaxChainedNeighborUpdates());
        this.world = world;
        this.fakeWorld = PolymerCommonUtils.getFakeWorld();
    }

    @Override
    public long getTickOrder() {
        return 0;
    }

    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler() {
        return fakeWorld.getBlockTickScheduler();
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
        return fakeWorld.getFluidTickScheduler();
    }

    @Override
    public WorldProperties getLevelProperties() {
        return this.world.getLevelProperties();
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
        return null;
    }

    @Override
    public LocalDifficulty getLocalDifficulty(BlockPos pos) {
        return this.world.getLocalDifficulty(pos);
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return this.world.getServer();
    }

    @Override
    public ChunkManager getChunkManager() {
        return fakeWorld.getChunkManager();
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    @Override
    public void playSound(@Nullable PlayerEntity except, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {

    }

    @Override
    public String asString() {
        return "";
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {

    }

    @Override
    public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {

    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return this.world.getBrightness(direction, shaded);
    }

    @Override
    public LightingProvider getLightingProvider() {
        return this.fakeWorld.getLightingProvider();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.world.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        var l = ChunkSectionPos.asLong(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getY()),
                ChunkSectionPos.getSectionCoord(pos.getZ())
        );
        var x = this.sections.get(l);

        return x != null
                ? x.get(ChunkSectionPos.getLocalCoord(pos.getX()), ChunkSectionPos.getLocalCoord(pos.getY()), ChunkSectionPos.getLocalCoord(pos.getZ()))
                : getClientState(l, pos);
    }

    private BlockState getClientState(long l, BlockPos pos) {
        var x = this.getRealContainer(l);

        return x != null ? getClientState(x.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15)) : Blocks.AIR.getDefaultState();
    }

    private BlockState getClientState(BlockState x) {
        var out = this.stateMap.get(x);
        if (out == null) {
            out = getContextState(x);
            this.stateMap.put(x, out);
        }
        return out;
    }

    protected abstract BlockState getContextState(BlockState x);

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
        return List.of();
    }

    @Override
    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate) {
        return List.of();
    }

    @Nullable
    @Override
    public Entity getEntityById(int id) {
        return null;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return List.of();
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
        if (this.isOutOfHeightLimit(pos)) {
            return false;
        }

        var current = this.getBlockState(pos);
        if (current == state) {
            return true;
        }

        var sectionLong = ChunkSectionPos.asLong(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getY()),
                ChunkSectionPos.getSectionCoord(pos.getZ())
        );

        maxUpdateDepth = Math.min(maxUpdateDepth, 128);
        var section = this.sections.get(sectionLong);

        if (section == null) {
            var original = this.getRealContainer(sectionLong);

            if (original == null) {
                return false;
            }

            section  = new PalettedContainer(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            var acc = ((PalettedContainerAccessor) section);
            var provider = acc.getPaletteProvider();

            for (int ix = 0; ix < 16; ix++) {
                for (int iy = 0; iy < 16; iy++) {
                    for (int iz = 0; iz < 16; iz++) {
                        acc.callSet(provider.computeIndex(ix, iy, iz), getClientState(original.get(ix, iy, iz)));
                    }
                }
            }

            this.sections.put(sectionLong, section);
        }

        var acc = ((PalettedContainerAccessor) section);
        acc.callSet(acc.getPaletteProvider().computeIndex(
                ChunkSectionPos.getLocalCoord(pos.getX()),
                ChunkSectionPos.getLocalCoord(pos.getY()),
                ChunkSectionPos.getLocalCoord(pos.getZ())
        ), state);

        if ((flags & Block.NOTIFY_NEIGHBORS) != 0) {
            this.updateNeighbors(pos, state.getBlock());
        }

        if ((flags & Block.FORCE_STATE) == 0 && maxUpdateDepth > 0) {
            int i = flags & -34;
            state.prepare(this, pos, i, maxUpdateDepth - 1);
            state.updateNeighbors(this, pos, i, maxUpdateDepth - 1);
            state.prepare(this, pos, i, maxUpdateDepth - 1);
        }

        return true;
    }

    protected PalettedContainer<BlockState> getRealContainer(long sectionLong) {
        if (this.realSections.containsKey(sectionLong)) {
            return this.realSections.get(sectionLong);
        }

        var chunk = world.getChunkManager().getWorldChunk(ChunkSectionPos.unpackX(sectionLong), ChunkSectionPos.unpackZ(sectionLong));
        if (chunk != null) {
            var i = chunk.sectionCoordToIndex(ChunkSectionPos.unpackY(sectionLong));

            if (i < chunk.getSectionArray().length && i >= 0) {
                var section = chunk.getSection(i);
                if (section != null) {
                    this.realSections.put(sectionLong, section.getBlockStateContainer());
                    return section.getBlockStateContainer();
                }
            }
        }
        this.realSections.put(sectionLong, null);
        return null;
    }

    @Override
    public void updateNeighbors(BlockPos pos, Block block) {
        this.neighborUpdater.updateNeighbors(pos, block, (Direction)null);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean move) {
        return false;
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
        return false;
    }

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
        return state.test(this.getBlockState(pos));
    }

    @Override
    public boolean testFluidState(BlockPos pos, Predicate<FluidState> state) {
        return state.test(this.getFluidState(pos));
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.world.getRecipeManager();
    }

    @Override
    public void addBlockEntity(BlockEntity blockEntity) {

    }

    @Override
    public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {

    }

    @Nullable
    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        return null;
    }

    @Override
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        return 0;
    }

    @Override
    public int getAmbientDarkness() {
        return 0;
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return this.world.getBiomeAccess();
    }

    @Override
    protected EntityLookup<Entity> getEntityLookup() {
        return ENTITY_LOOKUP;
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return this.world.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return this.world.getSeaLevel();
    }

    @Override
    public DimensionType getDimension() {
        return this.world.getDimension();
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.world.getRegistryManager();
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return this.world.getEnabledFeatures();
    }

    public void forEachNonMatchingState(DeltaConsumer consumer) {
        var mut = new BlockPos.Mutable();
        this.sections.forEach((csp, section) -> {
            var sectionPos = ChunkSectionPos.from(csp);

            int x = sectionPos.getMinX(), y = sectionPos.getMinY(), z = sectionPos.getMinZ();
            mut.set(x, y, z);

            var chunk = this.world.getChunk(mut);
            var realSection = getRealContainer(csp);

            for (int ix = 0; ix < 16; ix++) {
                for (int iy = 0; iy < 16; iy++) {
                    for (int iz = 0; iz < 16; iz++) {
                        var real = realSection.get(ix, iy, iz);
                        var virtual = section.get(ix, iy, iz);
                        if (real != virtual) {
                            mut.set(x + ix, y + iy, z + iz);
                            consumer.accept(mut, real, virtual);
                        }
                    }
                }
            }
        });
    }

    public void clear() {
        this.sections.clear();
        this.stateMap.clear();
    }
    public interface DeltaConsumer {
        void accept(BlockPos pos, BlockState realState, BlockState virtualState);
    }
}
