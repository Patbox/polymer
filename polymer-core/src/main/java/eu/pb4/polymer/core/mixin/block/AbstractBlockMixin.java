package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //noinspection ConstantValue
        if (((Object) this) instanceof Block block1 && PolymerSyncedObject.getSyncedObject(Registries.BLOCK, block1) instanceof PolymerBlock block) {
            var clientState = PolymerBlockUtils.getBlockStateSafely(block, state,
                    world instanceof World realWorld ? PacketContext.create(realWorld.getRegistryManager()) : PacketContext.create());
            if (!(PolymerSyncedObject.getSyncedObject(Registries.BLOCK, clientState.getBlock()) instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getOutlineShape(world, pos, context));
            }
        }
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceCollision(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //noinspection ConstantValue
        if (((Object) this) instanceof Block block1 && PolymerSyncedObject.getSyncedObject(Registries.BLOCK, block1) instanceof PolymerBlock block) {
            var clientState = context instanceof EntityShapeContext entityShapeContext
                    && entityShapeContext.getEntity() instanceof ServerPlayerEntity player && player.networkHandler != null
                    ? PolymerBlockUtils.getBlockStateSafely(block, state, PacketContext.create(player))
                    : PolymerBlockUtils.getBlockStateSafely(block, state, world instanceof World realWorld ? PacketContext.create(realWorld.getRegistryManager()) : PacketContext.create());
            if (!(PolymerSyncedObject.getSyncedObject(Registries.BLOCK, clientState.getBlock()) instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getCollisionShape(world, pos, context));
            }
        }
    }
}
