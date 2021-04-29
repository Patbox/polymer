package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Modified version of PolyMC mixin - https://github.com/TheEpicBlock/PolyMc/blob/master/src/main/java/io/github/theepicblock/polymc/mixins/block/BlockBreakingPatch.java

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerPlayerEntity player;
    @Shadow private int tickCounter;
    @Shadow private int startMiningTime;

    @Shadow
    public abstract void finishMining(BlockPos pos, PlayerActionC2SPacket.Action action, String reason);

    @Shadow public ServerWorld world;
    private int blockBreakingCooldown;


    @Inject(method = "continueMining", at = @At("TAIL"))
    private void breakIfTakingTooLong(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (state.getBlock() instanceof VirtualBlock || this.player.getMainHandStack().getItem() instanceof VirtualItem) {
            int j = this.tickCounter - i;
            float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float)(j);

            if (this.blockBreakingCooldown > 0) {
                --this.blockBreakingCooldown;
            }

            if (f >= 1.0F) {
                this.blockBreakingCooldown = 5;
                this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
                this.finishMining(pos, PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, "destroyed");

                if (!(state.getBlock() instanceof AbstractFireBlock)) {
                    this.world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state.getBlock() instanceof VirtualBlock ? ((VirtualBlock) state.getBlock()).getVirtualBlockState(state) : state));
                }

            }
        }
    }

    @Inject(method = "continueMining", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V"))
    private void onUpdateBreakStatus(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (state.getBlock() instanceof VirtualBlock || this.player.getMainHandStack().getItem() instanceof VirtualItem) {
            int j = tickCounter - i;
            float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float)(j + 1);
            int k = (int)(f * 10.0F);

            this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, k));
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void packetReceivedInject(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        if (this.player.getServerWorld().getBlockState(pos).getBlock() instanceof VirtualBlock || this.player.getMainHandStack().getItem() instanceof VirtualItem) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                this.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getEntityId(), new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, -1, true, false)));
            } else if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
                this.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(this.player.getEntityId(), StatusEffects.MINING_FATIGUE));
                if (this.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                    StatusEffectInstance effectInstance = player.getStatusEffect(StatusEffects.MINING_FATIGUE);
                    this.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getEntityId(), effectInstance));
                }
                this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
            }
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("TAIL"))
    private void enforceBlockBreakingCooldown(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        if (this.player.getServerWorld().getBlockState(pos).getBlock() instanceof VirtualBlock || this.player.getMainHandStack().getItem() instanceof VirtualItem) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                this.startMiningTime += blockBreakingCooldown;
            }
        }
    }

    @Inject(method = "finishMining", at = @At("HEAD"))
    private void clearEffects(BlockPos pos, PlayerActionC2SPacket.Action action, String reason, CallbackInfo ci) {
        if (this.player.getServerWorld().getBlockState(pos).getBlock() instanceof VirtualBlock || this.player.getMainHandStack().getItem() instanceof VirtualItem) {
            this.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(player.getEntityId(), StatusEffects.MINING_FATIGUE));
            if (this.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                StatusEffectInstance effectInstance = this.player.getStatusEffect(StatusEffects.MINING_FATIGUE);
                this.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getEntityId(), effectInstance));
            }
        }
    }


    @Redirect(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V"))
    private void noOneCaresAboutMismatch(Logger logger, String message) {}
}