package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.jspecify.annotations.Nullable;
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

@Mixin(LevelChunk.class)
public abstract class WorldChunkMixin extends ChunkAccess implements HolderAttachmentHolder {

    @Unique
    private final Collection<HolderAttachment> polymerVE$holders = new ArrayList<>();
    @Unique
    private final Map<BlockPos, BlockBoundAttachment> polymerVE$posHolders = new Object2ObjectOpenHashMap<>();
    @Shadow
    @Final
    private Level level;

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, PalettedContainerFactory palettesFactory, long inhabitedTime, @Nullable LevelChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, palettesFactory, inhabitedTime, sectionArray, blendingData);
    }

    @Shadow
    public abstract Level getLevel();

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V", at = @At("TAIL"))
    private void polymer$polymerBlocksInit(Level world, ChunkPos pos, UpgradeData upgradeData, LevelChunkTicks blockTickScheduler, LevelChunkTicks fluidTickScheduler, long inhabitedTime, LevelChunkSection[] sectionArrayInitializer, LevelChunk.PostLoadProcessor entityLoader, BlendingData blendingData, CallbackInfo ci) {
        if (world instanceof ServerLevel serverWorld) {
            var sections = this.getSections();
            for (int i = 0; i < sections.length; i++) {
                var section = sections[i];
                if (section != null && !section.hasOnlyAir()) {
                    var container = section.getStates();
                    if (container.maybeHas(x -> BlockWithElementHolder.get(x) != null)) {
                        BlockState state;
                        for (byte x = 0; x < 16; x++) {
                            for (byte z = 0; z < 16; z++) {
                                for (byte y = 0; y < 16; y++) {
                                    state = container.get(x, y, z);

                                    var blockWithElementHolder = BlockWithElementHolder.get(state);
                                    if (blockWithElementHolder != null) {
                                        var blockPos = pos.getBlockAt(x, this.getSectionYFromSectionIndex(i) * 16 + y, z);

                                        var holder = blockWithElementHolder.createElementHolder(serverWorld, blockPos, state);
                                        if (holder != null) {
                                            BlockBoundAttachment.of(holder, serverWorld, (LevelChunk) (Object) this, blockPos, state);
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

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z", ordinal = 0))
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

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z", ordinal = 1, shift = At.Shift.BEFORE))
    private void polymerVE$addNew(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        var x = this.polymerVE$posHolders.get(pos);
        var blockWithElementHolder = BlockWithElementHolder.get(state);
        if (x == null && blockWithElementHolder != null && this.level instanceof ServerLevel serverWorld) {
            var holder = blockWithElementHolder.createElementHolder(serverWorld, pos, state);
            if (holder != null) {
                new BlockBoundAttachment(holder, (LevelChunk) (Object) this, state, pos.immutable(), Vec3.atCenterOf(pos).add(blockWithElementHolder.getElementHolderOffset(serverWorld, pos, state)), blockWithElementHolder.tickElementHolder(serverWorld, pos, state));
            }
        }
    }

    @Inject(method = "setLoaded", at = @At("TAIL"))
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
