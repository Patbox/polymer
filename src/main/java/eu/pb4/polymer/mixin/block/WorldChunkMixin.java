package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.interfaces.WorldChunkInterface;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements WorldChunkInterface {
    private final Set<BlockPos> virtualBlocks = new HashSet<>();

    public WorldChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> registry, long l, @Nullable ChunkSection[] chunkSections, TickScheduler<Block> tickScheduler, TickScheduler<Fluid> tickScheduler2) {
        super(chunkPos, upgradeData, heightLimitView, registry, l, chunkSections, tickScheduler, tickScheduler2);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/TickScheduler;Lnet/minecraft/world/TickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Ljava/util/function/Consumer;)V",
            at = @At("TAIL")
    )
    private void polymer_virtualBlocksInit1(World world, ChunkPos pos, UpgradeData upgradeData, TickScheduler<Block> blockTickScheduler, TickScheduler<Fluid> fluidTickScheduler, long inhabitedTime, @Nullable ChunkSection[] sections, @Nullable Consumer<WorldChunk> loadToWorldConsumer, CallbackInfo info) {
        this.polymer_generateVirtualBlockSet();
    }

    @Inject(
            method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Ljava/util/function/Consumer;)V",
            at = @At("TAIL")
    )
    private void polymer_virtualBlocksInit2(ServerWorld serverWorld, ProtoChunk protoChunk, Consumer<WorldChunk> consumer, CallbackInfo ci) {
        this.polymer_generateVirtualBlockSet();
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;)V",
            at = @At("TAIL")
    )
    private void polymer_virtualBlocksInit3(World world, ChunkPos pos, CallbackInfo ci) {
        this.polymer_generateVirtualBlockSet();
    }


    @Unique
    private void polymer_generateVirtualBlockSet() {
        for (var section : this.getSectionArray()) {
            if (section != null && !section.isEmpty()) {
                var container = section.getContainer();
                if (container.hasAny(BlockHelper.IS_VIRTUAL_BLOCK_STATE_PREDICATE)) {
                    BlockState state;
                    for (byte x = 0; x < 16; x++) {
                        for (byte z = 0; z < 16; z++) {
                            for (byte y = 0; y < 16; y++) {
                                state = container.get(x, y, z);
                                if (state.getBlock() instanceof VirtualBlock) {
                                    this.virtualBlocks.add(new BlockPos(x + this.getPos().getStartX(), y + section.getYOffset(), z + this.getPos().getStartZ()));
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
        this.setVirtualBlock(pos, state);
    }


    public void setVirtualBlock(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof VirtualBlock) {
            this.virtualBlocks.add(pos.toImmutable());
        } else {
            this.virtualBlocks.remove(pos);
        }
    }

    public void removeVirtualBlock(BlockPos pos) {
        this.virtualBlocks.remove(pos);
    }

    public Set<BlockPos> getVirtualBlocks() {
        return this.virtualBlocks;
    }
}
