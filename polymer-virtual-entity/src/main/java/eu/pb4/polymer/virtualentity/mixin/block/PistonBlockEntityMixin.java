package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.impl.PistonExt;
import eu.pb4.polymer.virtualentity.impl.attachment.PistonAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonBlockEntityMixin extends BlockEntity implements PistonExt {
    public PistonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow public abstract BlockState getMovedState();

    @Nullable
    @Unique
    private PistonAttachment attachment;

    @Override
    public void polymer$setAttachement(PistonAttachment attachment) {
        this.attachment = attachment;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;moveCollidedEntities(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;FLnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;)V"))
    private static void updatePos(Level world, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci, @Local float progress) {
        var att = ((PistonBlockEntityMixin) (Object) blockEntity).attachment;
        if (att != null) {
            att.update(progress);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;updateFromNeighbourShapes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static void updatePos(Level world, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
        var att = ((PistonBlockEntityMixin) (Object) blockEntity).attachment;

        if (att != null) {
            att.update(1);
            BlockBoundAttachment.fromMoving(att.holder(), (ServerLevel) world, pos, blockEntity.getMovedState());
            ((PistonBlockEntityMixin) (Object) blockEntity).attachment = null;
        }
    }

    @Inject(method = "finalTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;updateFromNeighbourShapes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void updatePos(CallbackInfo ci) {
        var att = this.attachment;

        if (att != null) {
            att.update(1);
            BlockBoundAttachment.fromMoving(att.holder(), (ServerLevel) this.level, worldPosition, this.getMovedState());
            this.attachment = null;
        }
    }
}
