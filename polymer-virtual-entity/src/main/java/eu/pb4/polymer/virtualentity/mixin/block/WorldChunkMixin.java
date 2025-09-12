package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
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

    @Unique
    private final Collection<HolderAttachment> polymerVE$holders = new ArrayList<>();
    @Unique
    private final Map<BlockPos, BlockBoundAttachment> polymerVE$posHolders = new Object2ObjectOpenHashMap<>();
    @Shadow
    @Final
    private World world;

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, PalettesFactory palettesFactory, long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, palettesFactory, inhabitedTime, sectionArray, blendingData);
    }

    @Shadow
    public abstract World getWorld();

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;Lnet/minecraft/world/gen/chunk/BlendingData;)V", at = @At("TAIL"))
    private void polymer$polymerBlocksInit(World world, ChunkPos pos, UpgradeData upgradeData, ChunkTickScheduler blockTickScheduler, ChunkTickScheduler fluidTickScheduler, long inhabitedTime, ChunkSection[] sectionArrayInitializer, WorldChunk.EntityLoader entityLoader, BlendingData blendingData, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            var sections = this.getSectionArray();
            for (int i = 0; i < sections.length; i++) {
                var section = sections[i];
                if (section != null && !section.isEmpty()) {
                    var container = section.getBlockStateContainer();
                    if (container.hasAny(x -> BlockWithElementHolder.get(x) != null)) {
                        BlockState state;
                        for (byte x = 0; x < 16; x++) {
                            for (byte z = 0; z < 16; z++) {
                                for (byte y = 0; y < 16; y++) {
                                    state = container.get(x, y, z);

                                    var blockWithElementHolder = BlockWithElementHolder.get(state);
                                    if (blockWithElementHolder != null) {
                                        var blockPos = pos.getBlockPos(x, this.sectionIndexToCoord(i) * 16 + y, z);

                                        var holder = blockWithElementHolder.createElementHolder(serverWorld, blockPos, state);
                                        if (holder != null) {
                                            BlockBoundAttachment.of(holder, serverWorld, (WorldChunk) (Object) this, blockPos, state);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z", ordinal = 0))
    private void polymerVE$removeOld(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        var x = this.polymerVE$posHolders.get(pos);
        if (x != null) {
            if (x.getBlockState().getBlock() != state.getBlock()) {
                this.polymerVE$removePosHolder(pos);
            } else {
                x.setBlockState(state);
            }
        }
    }

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isClient()Z", ordinal = 1, shift = At.Shift.BEFORE))
    private void polymerVE$addNew(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        var x = this.polymerVE$posHolders.get(pos);
        var blockWithElementHolder = BlockWithElementHolder.get(state);
        if (x == null && blockWithElementHolder != null && this.world instanceof ServerWorld serverWorld) {
            var holder = blockWithElementHolder.createElementHolder(serverWorld, pos, state);
            if (holder != null) {
                new BlockBoundAttachment(holder, (WorldChunk) (Object) this, state, pos.toImmutable(), Vec3d.ofCenter(pos).add(blockWithElementHolder.getElementHolderOffset(serverWorld, pos, state)), blockWithElementHolder.tickElementHolder(serverWorld, pos, state));
            }
        }
    }

    @Inject(method = "setLoadedToWorld", at = @At("TAIL"))
    private void polymerVE$onChunkUnload(boolean loadedToWorld, CallbackInfo ci) {
        if (loadedToWorld) {
            return;
        }

        var holders = this.polymerVE$getHolders();
        if (!holders.isEmpty()) {
            var arr = holders.toArray(HolderHolder.HOLDER_ATTACHMENTS);
            for (int i = 0; i < arr.length; i++) {
                var holder = arr[i];
                if (holder != null) {
                    holder.destroy();
                }
            }
        }
    }

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
