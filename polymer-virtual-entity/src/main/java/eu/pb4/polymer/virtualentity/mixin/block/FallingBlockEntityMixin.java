package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.impl.attachment.FallingBlockEntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    public FallingBlockEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow public abstract BlockState getBlockState();

    @Nullable
    @Unique
    private FallingBlockEntityAttachment attachment;

    @Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.BEFORE))
    private static void getCurrentAttachment(Level world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir, @Local FallingBlockEntity entity,
                                             @Share("holder") LocalRef<ElementHolder> ref) {
        var x = BlockBoundAttachment.get(world, pos);
        if (x != null) {
            var holder = BlockWithElementHolder.get(x.getBlockState());
            if (holder != null) {
                var transformed = holder.createMovingElementHolder((ServerLevel) world, pos, x.getBlockState(), x.holder());

                if (transformed != null) {
                    if (transformed == x.holder()) {
                        x.holder().setAttachment(null);
                        x.destroy();
                    }
                    ref.set(transformed);
                }
            }
        }
    }

    @Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.AFTER))
    private static void attach(Level world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir, @Local FallingBlockEntity entity,
                                             @Share("holder") LocalRef<ElementHolder> ref) {
        var x = ref.get();
        if (x != null)  {
            ((FallingBlockEntityMixin) (Object) entity).attachment = new FallingBlockEntityAttachment(x, entity);
        }
    }


    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.BEFORE))
    private void updatePos(CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos) {
        var att = this.attachment;

        if (att != null) {
            BlockBoundAttachment.fromMoving(att.holder(), (ServerLevel) this.level(), blockPos, this.getBlockState());
        }
    }
}
