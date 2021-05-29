package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.block.VirtualHeadBlock;
import eu.pb4.polymer.interfaces.WorldChunkInterface;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements WorldChunkInterface {
    @Shadow @Final private World world;

    @Shadow public abstract ChunkPos getPos();

    @Shadow @Final private ChunkSection[] sections;
    private Set<BlockPos> virtualBlocks = new HashSet<>();

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/biome/source/BiomeArray;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/TickScheduler;Lnet/minecraft/world/TickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Ljava/util/function/Consumer;)V",
            at = @At("TAIL")
    )
    private void virtualHeadBlocksInit1(World world, ChunkPos pos, BiomeArray biomes, UpgradeData upgradeData, TickScheduler<Block> blockTickScheduler, TickScheduler<Fluid> fluidTickScheduler, long inhabitedTime, @Nullable ChunkSection[] sections, @Nullable Consumer<WorldChunk> loadToWorldConsumer, CallbackInfo info) {
        this.generateVirtualBlockSet();
    }

    @Inject(
            method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Ljava/util/function/Consumer;)V",
            at = @At("TAIL")
    )
    private void virtualBlocksInit2(ServerWorld serverWorld, ProtoChunk protoChunk, Consumer<WorldChunk> consumer, CallbackInfo ci) {
        this.generateVirtualBlockSet();
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/biome/source/BiomeArray;)V",
            at = @At("TAIL")
    )
    private void virtualBlocksInit3(World world, ChunkPos pos, BiomeArray biomes, CallbackInfo ci) {
        this.generateVirtualBlockSet();
    }


    @Unique
    private void generateVirtualBlockSet() {
        for (byte x = 0; x < 16; x++) {
            for (byte z = 0; z < 16; z++) {
                for (int y = 0; y < this.world.getHeight(); y++) {
                    BlockPos blockPos = new BlockPos(x + this.getPos().getStartX(), y, z + this.getPos().getStartZ());
                    ChunkSection chunkSection = this.sections[y >> 4];
                    if (!ChunkSection.isEmpty(chunkSection)) {
                        BlockState blockState =  chunkSection.getBlockState(x, y & 15, z);
                        if (blockState.getBlock() instanceof VirtualBlock) {
                            this.virtualBlocks.add(blockPos);
                        }
                    }
                }
            }
        }
    }


    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;setBlockState(IIILnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;", shift = At.Shift.AFTER))
    private void addToList(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        this.setVirtualBlock(pos, state);
    }


    public void setVirtualBlock(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof VirtualBlock) {
            this.virtualBlocks.add(pos);
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
