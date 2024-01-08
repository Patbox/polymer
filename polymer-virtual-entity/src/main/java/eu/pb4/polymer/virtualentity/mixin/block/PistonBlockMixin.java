package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.PistonExt;
import eu.pb4.polymer.virtualentity.impl.attachment.PistonAttachment;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/BlockPos;offset(Lnet/minecraft/util/math/Direction;)Lnet/minecraft/util/math/BlockPos;", ordinal = 1, shift = At.Shift.BEFORE))
    private void collectAttachmentHolder(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir,
                                         @Local(ordinal = 2) BlockPos blockPos, @Share("attachment") LocalRef<PistonAttachment> attachment) {
        if (world instanceof ServerWorld serverWorld ) {
            var x = BlockBoundAttachment.get(world, blockPos);
            if (x != null && x.getBlockState().getBlock() instanceof BlockWithElementHolder holder) {
                var transformed = holder.createMovingElementHolder(serverWorld, blockPos, x.getBlockState(), x.holder());

                if (transformed != null) {
                    if (transformed == x.holder()) {
                        x.destroy();
                    }
                    attachment.set(new PistonAttachment(transformed, world.getWorldChunk(blockPos), x.getBlockState(), blockPos, retract ? dir : dir.getOpposite()));
                }
            }
        }
    }

    @ModifyArg(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;addBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V", ordinal = 0))
    private BlockEntity collectAttachmentHolder(BlockEntity blockEntity, @Share("attachment") LocalRef<PistonAttachment> attachment) {
        var val = attachment.get();
        if (val != null) {
            ((PistonExt) blockEntity).polymer$setAttachement(val);
        }
        return blockEntity;
    }
}
