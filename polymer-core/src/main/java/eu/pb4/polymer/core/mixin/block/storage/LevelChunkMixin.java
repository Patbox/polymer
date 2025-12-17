package eu.pb4.polymer.core.mixin.block.storage;

import com.google.common.collect.ForwardingIterator;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements PolymerBlockPosStorage {

    public LevelChunkMixin(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, PalettedContainerFactory palettesFactory, long inhabitedTime, @Nullable LevelChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, palettesFactory, inhabitedTime, sectionArray, blendingData);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V",
            at = @At("TAIL")
    )
    private void polymer$polymerBlocksInit(Level world, ChunkPos pos, UpgradeData upgradeData, LevelChunkTicks blockTickScheduler, LevelChunkTicks fluidTickScheduler, long inhabitedTime, LevelChunkSection[] sectionArrayInitializer, LevelChunk.PostLoadProcessor entityLoader, BlendingData blendingData, CallbackInfo ci) {
        if (world instanceof ServerLevel) {
            this.polymer$generatePolymerBlockSet();
        }
    }


    @Unique
    private void polymer$generatePolymerBlockSet() {
        for (var section : this.getSections()) {
            if (section != null && !section.hasOnlyAir()) {
                var container = section.getStates();
                if (container.maybeHas(PolymerImplUtils.POLYMER_STATES::contains)) {
                    var storage = (PolymerBlockPosStorage) section;
                    BlockState state;
                    for (byte x = 0; x < 16; x++) {
                        for (byte z = 0; z < 16; z++) {
                            for (byte y = 0; y < 16; y++) {
                                state = container.get(x, y, z);
                                if (PolymerImplUtils.POLYMER_STATES.contains(state)) {
                                    storage.polymer$setSynced(x, y, z, PolymerBlockUtils.forceLightUpdates(state));
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.AFTER))
    private void polymer$addToList(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        if (PolymerImplUtils.POLYMER_STATES.contains(state)) {
            this.polymer$setSynced(pos.getX(), pos.getY(), pos.getZ(), PolymerBlockUtils.forceLightUpdates(state));
        } else {
            this.polymer$removeSynced(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public @Nullable Iterator<BlockPos.MutableBlockPos> polymer$iterator() {
        return new ForwardingIterator<>() {
            int current;
            Iterator<BlockPos.MutableBlockPos> currentIterator = Collections.emptyIterator();

            @Override
            protected Iterator<BlockPos.MutableBlockPos> delegate() {
                if (this.currentIterator == null || !this.currentIterator.hasNext()) {
                    var array = LevelChunkMixin.this.getSections();
                    while (this.current < array.length) {
                        var id = this.current++;
                        var s = array[id];
                        var si = (PolymerBlockPosStorage) s;
                        if (s != null && si.polymer$hasAny()) {
                            this.currentIterator = si.polymer$iterator(SectionPos.of(LevelChunkMixin.this.getPos(), LevelChunkMixin.this.getSectionYFromSectionIndex(id)));
                            break;
                        }
                    }
                }

                return this.currentIterator;
            }
        };
    }

    @Override
    public void polymer$setSynced(int x, int y, int z, boolean lightSource) {
        this.polymer_getSectionStorage(y).polymer$setSynced(x, y, z, lightSource);
    }

    @Override
    public void polymer$removeSynced(int x, int y, int z) {
        this.polymer_getSectionStorage(y).polymer$removeSynced(x, y, z);
    }

    @Override
    public boolean polymer$isSynced(int x, int y, int z) {
        return this.polymer_getSectionStorage(y).polymer$isSynced(x, y, z);
    }

    @Override
    public boolean polymer$hasAny() {
        for (var s : this.getSections()) {
            if (s != null && ((PolymerBlockPosStorage) s).polymer$hasAny()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable ShortSet polymer$getBackendSet() {
        return null;
    }

    @Override
    public @Nullable Iterator<BlockPos.MutableBlockPos> polymer$iterator(SectionPos sectionPos) {
        return null;
    }

    private PolymerBlockPosStorage polymer_getSectionStorage(int y) {
        return (PolymerBlockPosStorage) this.getSection(this.getSectionIndex(y));
    }
}
