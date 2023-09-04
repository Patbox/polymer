package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements HolderAttachmentHolder {

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    @Shadow
    public abstract World getWorld();

    @Shadow @Final private World world;
    @Unique
    private final Collection<HolderAttachment> polymerVE$holders = new ArrayList<>();

    @Unique
    private final Map<BlockPos, BlockBoundAttachment> polymerVE$posHolders = new Object2ObjectOpenHashMap<>();

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
            at = @At("TAIL")
    )
    private void polymer$polymerBlocksInit(World world, ChunkPos pos, UpgradeData upgradeData, ChunkTickScheduler blockTickScheduler, ChunkTickScheduler fluidTickScheduler, long inhabitedTime, ChunkSection[] sectionArrayInitializer, WorldChunk.EntityLoader entityLoader, BlendingData blendingData, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            var sections = this.getSectionArray();
            for (int i = 0; i < sections.length; i++) {
                var section = sections[i];
                if (section != null && !section.isEmpty()) {
                    var container = section.getBlockStateContainer();
                    if (container.hasAny(x -> x.getBlock() instanceof BlockWithElementHolder)) {
                        BlockState state;
                        for (byte x = 0; x < 16; x++) {
                            for (byte z = 0; z < 16; z++) {
                                for (byte y = 0; y < 16; y++) {
                                    state = container.get(x, y, z);

                                    if (state.getBlock() instanceof BlockWithElementHolder blockWithElementHolder) {
                                        var blockPos = pos.getBlockPos(x, this.sectionIndexToCoord(i) * 16 + y, z);

                                        var holder = blockWithElementHolder.createElementHolder(serverWorld, blockPos, state);
                                        if (holder != null) {
                                            new BlockBoundAttachment(holder, (WorldChunk) (Object) this, state, blockPos, Vec3d.ofCenter(blockPos).add(blockWithElementHolder.getElementHolderOffset(serverWorld, blockPos, state)), blockWithElementHolder.tickElementHolder(serverWorld, blockPos, state));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }        }
    }

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onStateReplaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymerVE$removeOld(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir,
                                     int i, ChunkSection section, boolean bool, int j, int k, int l, BlockState oldBlockState) {
        if (oldBlockState.getBlock() instanceof BlockWithElementHolder) {
            if (oldBlockState.getBlock() != state.getBlock()) {
                this.polymerVE$removePosHolder(pos);
            } else {
                var x = this.polymerVE$posHolders.get(pos);
                if (x != null) {
                    x.setBlockState(state);
                }
            }
        }
    }

    @Inject(method = "setBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z", ordinal = 1, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymerVE$addNew(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir,
                                     int i, ChunkSection section, boolean bool, int j, int k, int l, BlockState oldBlockState) {
        if (oldBlockState.getBlock() != state.getBlock() && state.getBlock() instanceof BlockWithElementHolder blockWithElementHolder && this.world instanceof ServerWorld serverWorld) {
            var holder = blockWithElementHolder.createElementHolder(serverWorld, pos, state);
            if (holder != null) {
                new BlockBoundAttachment(holder, (WorldChunk) (Object) this, state, pos.toImmutable(), Vec3d.ofCenter(pos).add(blockWithElementHolder.getElementHolderOffset(serverWorld, pos, state)), blockWithElementHolder.tickElementHolder(serverWorld, pos, state));
            }
        }
    };

    @Override
    public void polymerVE$addHolder(HolderAttachment holderAttachment) {
        this.polymerVE$holders.add(holderAttachment);
        if (holderAttachment instanceof BlockBoundAttachment blockBoundAttachment) {
            this.polymerVE$posHolders.put(blockBoundAttachment.getBlockPos(), blockBoundAttachment);
        }
    }

    @Override
    public void polymerVE$removeHolder(HolderAttachment holderAttachment) {
        this.polymerVE$holders.remove(holderAttachment);
        if (holderAttachment instanceof BlockBoundAttachment blockBoundAttachment) {
            this.polymerVE$posHolders.remove(blockBoundAttachment);
        }
    }

    @Override
    public BlockBoundAttachment polymerVE$getPosHolder(BlockPos pos) {
        return this.polymerVE$posHolders.get(pos);
    }

    @Override
    public void polymerVE$removePosHolder(BlockPos pos) {
        var x = this.polymerVE$posHolders.remove(pos);
        if (x != null) {
            this.polymerVE$holders.remove(x);
            x.destroy();
        }
    }

    @Override
    public Collection<HolderAttachment> polymerVE$getHolders() {
        return this.polymerVE$holders;
    }
}
