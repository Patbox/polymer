package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (this instanceof PolymerBlock block) {
            var clientState = PolymerBlockUtils.getBlockStateSafely(block, state, PacketContext.of());
            if (!(clientState.getBlock() instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getOutlineShape(world, pos, context));
            }
        }
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceCollision(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (this instanceof PolymerBlock block) {
            var clientState = context instanceof EntityShapeContext entityShapeContext
                    && entityShapeContext.getEntity() instanceof ServerPlayerEntity player
                    ? PolymerBlockUtils.getBlockStateSafely(block, state, PacketContext.of(player))
                    : PolymerBlockUtils.getBlockStateSafely(block, state, PacketContext.of());
            if (!(clientState.getBlock() instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getCollisionShape(world, pos, context));
            }
        }
    }
}
