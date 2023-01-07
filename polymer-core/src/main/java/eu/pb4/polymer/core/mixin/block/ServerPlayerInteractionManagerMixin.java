package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;
import org.slf4j.Logger;
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

    @Shadow public abstract void finishMining(BlockPos pos, int sequence, String reason);

    @Unique
    private int polymer$sequence = 0;

    @Unique
    private int polymer$blockBreakingCooldown;

    @Unique
    private boolean polymer$hasMiningFatigue;

    @Inject(method = "continueMining", at = @At("TAIL"))
    private void polymer_breakIfTakingTooLong(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (this.polymer$shouldMineServerSide(pos, state)) {
            int j = this.tickCounter - i;
            float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float) (j);

            if (this.polymer$blockBreakingCooldown > 0) {
                --this.polymer$blockBreakingCooldown;
            }

            if (f >= 1.0F) {
                this.polymer$blockBreakingCooldown = 5;
                this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
                this.finishMining(pos, this.polymer$sequence, "destroyed");

                if (!(state.getBlock() instanceof AbstractFireBlock)) {
                    this.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                }

            }
        } else if (this.polymer$hasMiningFatigue) {
            this.polymer$clearMiningEffect();
        }
    }

    @Inject(method = "continueMining", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V"))
    private void polymer$onUpdateBreakStatus(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (this.polymer$shouldMineServerSide(pos, state)) {
            int j = tickCounter - i;
            float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float) (j + 1);
            int k = (int) (f * 10.0F);

            this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, k));
        } else if (this.polymer$hasMiningFatigue) {
            this.polymer$clearMiningEffect();
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void polymer_packetReceivedInject(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        this.polymer$sequence = sequence;
        var state = this.player.getWorld().getBlockState(pos);
        if (this.polymer$shouldMineServerSide(pos, state)) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                var ogDelta = state.calcBlockBreakingDelta(this.player, this.world, pos);;
                if (state.getBlock() instanceof PolymerBlock virtualBlock) {
                    state = PolymerBlockUtils.getBlockStateSafely(virtualBlock, state);
                }

                float delta = state.calcBlockBreakingDelta(this.player, this.world, pos);

                if (delta >= 1.0f && ogDelta < 1.0f) {
                    this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, state));
                }

                if (ogDelta < 1.0f) {
                    polymer$sendMiningFatigue();
                }
            } else if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
                if (this.polymer$hasMiningFatigue) {
                    this.polymer$clearMiningEffect();
                }
                this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
            }
        } else if (this.polymer$hasMiningFatigue) {
            this.polymer$clearMiningEffect();
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("TAIL"))
    private void polymer$enforceBlockBreakingCooldown(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (this.polymer$shouldMineServerSide(pos, this.player.getWorld().getBlockState(pos))) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                this.startMiningTime += polymer$blockBreakingCooldown;
            }
        } else if (this.polymer$hasMiningFatigue) {
            this.polymer$clearMiningEffect();
        }
    }

    @Inject(method = "finishMining", at = @At("HEAD"))
    private void polymer$clearEffects(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
        this.polymer$clearMiningEffect();
    }

    @Unique
    private boolean polymer$shouldMineServerSide(BlockPos pos, BlockState state) {
        return (state.getBlock() instanceof PolymerBlock block && block.handleMiningOnServer(player.getMainHandStack(), state, pos, this.player))
                || (this.player.getMainHandStack().getItem() instanceof PolymerItem item && item.handleMiningOnServer(player.getMainHandStack(), state, pos, player))
                || PolymerBlockUtils.SERVER_SIDE_MINING_CHECK.invoke((x) -> x.onBlockMine(state, pos, this.player));
    }

    @Unique
    private void polymer$sendMiningFatigue() {
        this.polymer$hasMiningFatigue = true;
        this.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getId(), new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, -1, true, false)));
    }

    @Unique
    private void polymer$clearMiningEffect() {
        this.polymer$hasMiningFatigue = false;
        this.player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(player.getId(), StatusEffects.MINING_FATIGUE));
        if (this.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            StatusEffectInstance effectInstance = this.player.getStatusEffect(StatusEffects.MINING_FATIGUE);
            this.player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getId(), effectInstance));
        }
    }

    @Redirect(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), require = 0)
    private void polymer$noOneCaresAboutMismatch(Logger instance, String s, Object o, Object o2) {
    }
}