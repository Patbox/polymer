package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow
    public abstract Block getBlock();

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceCollision(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //noinspection ConstantValue
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, this.getBlock()) instanceof PolymerBlock block
                && context instanceof EntityCollisionContext entityShapeContext
                && entityShapeContext.getEntity() instanceof ServerPlayer player && block.overridePlayerCollisionsWithPolymer(level, pos, (BlockState) (Object) this, player)) {
            var clientState =  PolymerBlockUtils.getBlockStateSafely(block, (BlockState) (Object) this, player.connection.getPacketContext());
            if (!(PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, clientState.getBlock()) instanceof PolymerBlock)) {
                cir.setReturnValue(clientState.getCollisionShape(level, pos, context));
            }
        }
    }
}
