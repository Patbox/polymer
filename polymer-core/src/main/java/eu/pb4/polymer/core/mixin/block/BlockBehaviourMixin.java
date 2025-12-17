package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //noinspection ConstantValue
        if (((Object) this) instanceof Block block1 && PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, block1) instanceof PolymerBlock block) {
            var clientState = PolymerBlockUtils.getBlockStateSafely(block, state,
                    world instanceof Level realWorld ? PacketContext.create(realWorld.registryAccess()) : PacketContext.create());
            if (!(PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, clientState.getBlock()) instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getShape(world, pos, context));
            }
        }
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceCollision(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //noinspection ConstantValue
        if (((Object) this) instanceof Block block1 && PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, block1) instanceof PolymerBlock block) {
            var clientState = context instanceof EntityCollisionContext entityShapeContext
                    && entityShapeContext.getEntity() instanceof ServerPlayer player && player.connection != null
                    ? PolymerBlockUtils.getBlockStateSafely(block, state, PacketContext.create(player))
                    : PolymerBlockUtils.getBlockStateSafely(block, state, world instanceof Level realWorld ? PacketContext.create(realWorld.registryAccess()) : PacketContext.create());
            if (!(PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, clientState.getBlock()) instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getCollisionShape(world, pos, context));
            }
        }
    }
}
