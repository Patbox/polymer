package eu.pb4.polymer.mixin.block.storage;

import com.google.common.collect.ForwardingIterator;
import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.impl.interfaces.PolymerBlockPosStorage;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements PolymerBlockPosStorage {

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable Blender blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/chunk/WorldChunk$class_6829;Lnet/minecraft/world/gen/chunk/Blender;)V",
            at = @At("TAIL")
    )
    private void polymer_polymerBlocksInit(World world, ChunkPos pos, UpgradeData upgradeData, ChunkTickScheduler blockTickScheduler, ChunkTickScheduler fluidTickScheduler, long inhabitedTime, ChunkSection[] sectionArrayInitializer, WorldChunk.class_6829 arg, Blender blendingData, CallbackInfo ci) {
        this.polymer_generatePolymerBlockSet();
    }


    @Unique
    private void polymer_generatePolymerBlockSet() {
        for (var section : this.getSectionArray()) {
            if (section != null && !section.isEmpty()) {
                var container = section.getBlockStateContainer();
                if (container.hasAny(PolymerBlockUtils.IS_POLYMER_BLOCK_STATE_PREDICATE)) {
                    var storage = (PolymerBlockPosStorage) section;
                    BlockState state;
                    for (byte x = 0; x < 16; x++) {
                        for (byte z = 0; z < 16; z++) {
                            for (byte y = 0; y < 16; y++) {
                                state = container.get(x, y, z);
                                if (state.getBlock() instanceof PolymerBlock) {
                                    storage.polymer_setPolymer(x, y, z);
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;setBlockState(IIILnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;", shift = At.Shift.AFTER))
    private void polymer_addToList(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        if (state.getBlock() instanceof PolymerBlock) {
            this.polymer_setPolymer(pos.getX(), pos.getY(), pos.getZ());
        } else {
            this.polymer_removePolymer(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public @Nullable Iterator<BlockPos.Mutable> polymer_iterator() {
        return new ForwardingIterator<>() {
            int current;
            Iterator<BlockPos.Mutable> currentIterator = Collections.emptyIterator();

            @Override
            protected Iterator<BlockPos.Mutable> delegate() {
                if (this.currentIterator == null || !this.currentIterator.hasNext()) {
                    var array = WorldChunkMixin.this.getSectionArray();
                    while (this.current < array.length) {
                        var s = array[this.current++];
                        var si = (PolymerBlockPosStorage) s;
                        if (s != null && si.polymer_hasAny()) {
                            this.currentIterator = si.polymer_iterator(ChunkSectionPos.from(WorldChunkMixin.this.getPos(), s.getYOffset() >> 4));
                            break;
                        }
                    }
                }

                return this.currentIterator;
            }
        };
    }

    @Override
    public void polymer_setPolymer(int x, int y, int z) {
        this.polymer_getSectionStorage(y).polymer_setPolymer(x, y, z);
    }

    @Override
    public void polymer_removePolymer(int x, int y, int z) {
        this.polymer_getSectionStorage(y).polymer_removePolymer(x, y, z);
    }

    @Override
    public boolean polymer_getPolymer(int x, int y, int z) {
        return this.polymer_getSectionStorage(y).polymer_getPolymer(x, y, z);
    }

    @Override
    public boolean polymer_hasAny() {
        for (var s : this.getSectionArray()) {
            if (s != null && ((PolymerBlockPosStorage) s).polymer_hasAny()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable ShortSet polymer_getBackendSet() {
        return null;
    }

    @Override
    public @Nullable Iterator<BlockPos.Mutable> polymer_iterator(ChunkSectionPos sectionPos) {
        return null;
    }

    private PolymerBlockPosStorage polymer_getSectionStorage(int y) {
        return (PolymerBlockPosStorage) this.getSection(this.getSectionIndex(y));
    }
}
