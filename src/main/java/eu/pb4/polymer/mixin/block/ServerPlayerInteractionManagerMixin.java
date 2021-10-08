package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Part of this mixin is based on https://github.com/TheEpicBlock/PolyMc/blob/master/src/main/java/io/github/theepicblock/polymc/mixins/block/BlockBreakingPatch.java

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow
    protected ServerPlayerEntity player;
    @Shadow
    protected ServerWorld world;
    @Shadow
    private int tickCounter;
    @Shadow
    private int startMiningTime;
    @Unique
    private int blockBreakingCooldown;

    @Shadow
    public abstract void finishMining(BlockPos pos, PlayerActionC2SPacket.Action action, String reason);

    @Inject(method = "continueMining", at = @At("TAIL"))
    private void polymer_breakIfTakingTooLong(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (this.polymer_shouldMineServerSide(pos, state)) {
            int j = this.tickCounter - i;
            float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float) (j);

            if (this.blockBreakingCooldown > 0) {
                --this.blockBreakingCooldown;
            }

            if (f >= 1.0F) {
                this.blockBreakingCooldown = 5;
                this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
                this.finishMining(pos, PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, "destroyed");

                if (!(state.getBlock() instanceof AbstractFireBlock)) {
                    this.world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state.getBlock() instanceof VirtualBlock virtualBlock ? BlockHelper.getBlockStateSafely(virtualBlock, state) : state));
                }

            }
        }
    }

    @Inject(method = "continueMining", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V"))
    private void polymer_onUpdateBreakStatus(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (this.polymer_shouldMineServerSide(pos, state)) {
            int j = tickCounter - i;
            float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float) (j + 1);
            int k = (int) (f * 10.0F);

            this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, k));
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void polymer_packetReceivedInject(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        var state = this.player.getServerWorld().getBlockState(pos);
        if (this.polymer_shouldMineServerSide(pos, state)) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                if (state.getBlock() instanceof VirtualBlock virtualBlock) {
                    state = BlockHelper.getBlockStateSafely(virtualBlock, state);
                }

                float delta = state.calcBlockBreakingDelta(this.player, this.world, pos);

                if (delta < 1.0f) {
                    this.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getId(), new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, -1, true, false)));
                }
            } else if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
                this.polymer_clearMiningEffect();
                this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
            }
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("TAIL"))
    private void polymer_enforceBlockBreakingCooldown(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        if (this.polymer_shouldMineServerSide(pos, this.player.getServerWorld().getBlockState(pos))) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                this.startMiningTime += blockBreakingCooldown;
            }
        }
    }

    @Inject(method = "finishMining", at = @At("HEAD"))
    private void polymer_clearEffects(BlockPos pos, PlayerActionC2SPacket.Action action, String reason, CallbackInfo ci) {
        this.polymer_clearMiningEffect();
    }


    private boolean polymer_shouldMineServerSide(BlockPos pos, BlockState state) {
        return state.getBlock() instanceof VirtualBlock || this.player.getMainHandStack().getItem() instanceof VirtualItem || BlockHelper.SERVER_SIDE_MINING_CHECK.invoke(this.player, pos, state);
    }

    private void polymer_clearMiningEffect() {
        this.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(player.getId(), StatusEffects.MINING_FATIGUE));
        if (this.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            StatusEffectInstance effectInstance = this.player.getStatusEffect(StatusEffects.MINING_FATIGUE);
            this.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getId(), effectInstance));
        }
    }

    @Redirect(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), require = 0)
    private void polymer_noOneCaresAboutMismatch(Logger logger, String message, Object p0, Object p1) {
    }
}