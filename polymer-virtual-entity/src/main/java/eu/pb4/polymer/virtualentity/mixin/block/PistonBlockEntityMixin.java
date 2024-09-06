package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.impl.PistonExt;
import eu.pb4.polymer.virtualentity.impl.attachment.PistonAttachment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin extends BlockEntity implements PistonExt {
    public PistonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow public abstract BlockState getPushedBlock();

    @Nullable
    @Unique
    private PistonAttachment attachment;

    @Override
    public void polymer$setAttachement(PistonAttachment attachment) {
        this.attachment = attachment;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;pushEntities(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FLnet/minecraft/block/entity/PistonBlockEntity;)V"))
    private static void updatePos(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity, CallbackInfo ci, @Local float progress) {
        var att = ((PistonBlockEntityMixin) (Object) blockEntity).attachment;
        if (att != null) {
            att.update(progress);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;postProcessState(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private static void updatePos(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity, CallbackInfo ci) {
        var att = ((PistonBlockEntityMixin) (Object) blockEntity).attachment;

        if (att != null) {
            att.update(1);
            BlockBoundAttachment.fromMoving(att.holder(), (ServerWorld) world, pos, blockEntity.getPushedBlock());
            ((PistonBlockEntityMixin) (Object) blockEntity).attachment = null;
        }
    }

    @Inject(method = "finish", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;postProcessState(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private void updatePos(CallbackInfo ci) {
        var att = this.attachment;

        if (att != null) {
            att.update(1);
            BlockBoundAttachment.fromMoving(att.holder(), (ServerWorld) this.world, pos, this.getPushedBlock());
            this.attachment = null;
        }
    }
}
