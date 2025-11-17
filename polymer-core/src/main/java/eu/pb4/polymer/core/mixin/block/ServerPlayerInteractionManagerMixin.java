package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Objects;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Final
    @Shadow
    protected ServerPlayerEntity player;
    @Shadow
    protected ServerWorld world;
    @Shadow
    private int startMiningTime;
    @Shadow public abstract void finishMining(BlockPos pos, int sequence, String reason);

    @Unique
    private int polymer$sequence = 0;

    @Unique
    private float polymer$currentBreakingProgress;

    @Unique
    private int polymer$blockBreakingCooldown;

    @Unique
    private boolean polymer$hasMiningFatigue;

    @Unique
    @Nullable
    private BlockPos polymer$currentlyMinedPos;

    @Unique
    @Nullable
    private BlockState polymer$currentlyMinedState;

    @Inject(method = "continueMining", at = @At("TAIL"))
    private void polymer_breakIfTakingTooLong(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (this.polymer$shouldMineServerSide(pos, state)) {
            if (!pos.equals(this.polymer$currentlyMinedPos) && this.polymer$currentlyMinedPos != null) {
                PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, this.polymer$currentlyMinedPos, this.polymer$currentlyMinedState, -1));
            }

            this.polymer$currentlyMinedState = state;
            this.polymer$currentlyMinedPos = pos;
            if (this.polymer$blockBreakingCooldown > 0) {
                --this.polymer$blockBreakingCooldown;
                return;
            }
            this.polymer$currentBreakingProgress += state.calcBlockBreakingDelta(this.player, this.player.getEntityWorld(), pos);

            if (this.polymer$currentBreakingProgress >= 1.0F) {
                this.polymer$blockBreakingCooldown = 5;
                this.polymer$currentBreakingProgress = 0;
                this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
                this.finishMining(pos, this.polymer$sequence, "destroyed");
                this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, pos));
                PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, pos, state, -1));
                this.polymer$currentlyMinedState = null;
                this.polymer$currentlyMinedPos = null;
            } else {
                var k = this.polymer$currentBreakingProgress > 0.0F ? (int)(this.polymer$currentBreakingProgress * 10) : -1;
                this.player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, k));
                polymer$sendMiningFatigue();
                PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, pos, state, k));
            }
        } else if (this.polymer$hasMiningFatigue) {
            this.polymer$clearMiningEffect();
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void polymer_packetReceivedInject(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        this.polymer$sequence = sequence;
        var serverState = this.player.getEntityWorld().getBlockState(pos);
        if (this.polymer$shouldMineServerSide(pos, serverState)) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                this.polymer$currentBreakingProgress = 0;
                this.polymer$currentlyMinedState = serverState;
                this.polymer$currentlyMinedPos = pos;
                var serverDelta = serverState.calcBlockBreakingDelta(this.player, this.world, pos);
                var clientState = serverState;
                if (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, serverState.getBlock()) instanceof PolymerBlock virtualBlock) {
                    clientState = PolymerBlockUtils.getBlockStateSafely(virtualBlock, serverState, PacketContext.create(this.player));
                }

                float clientDelta = clientState.calcBlockBreakingDelta(this.player, this.world, pos);

                if (clientDelta >= 1.0f && serverDelta < 1.0f) {
                    this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, serverState));
                }
                PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, pos, serverState, 0));

                if (serverDelta < 1.0f) {
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

    @Inject(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V", ordinal = 0))
    private void polymer$clearBreakingTime(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        this.polymer$currentBreakingProgress = 0;
    }

    @Inject(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V", ordinal = 1))
    private void polymer$clearBreakingTimeInstaMine(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        this.polymer$currentBreakingProgress = 0;
    }

    @Inject(method = "processBlockBreakingAction", at = @At("TAIL"))
    private void polymer$enforceBlockBreakingCooldown(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (this.polymer$shouldMineServerSide(pos, this.player.getEntityWorld().getBlockState(pos))) {
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
        return PolymerBlockUtils.shouldMineServerSide(this.player, pos, state);
    }

    @Unique
    private void polymer$sendMiningFatigue() {
        this.polymer$hasMiningFatigue = true;
        var x = new EntityAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED, (a) -> {});
        x.setBaseValue(-9999);
        this.player.networkHandler.sendPacket(new EntityAttributesS2CPacket(this.player.getId(), List.of(x)));
    }

    @Unique
    private void polymer$clearMiningEffect() {
        this.polymer$hasMiningFatigue = false;
        this.player.networkHandler.sendPacket(new EntityAttributesS2CPacket(this.player.getId(),
                List.of(Objects.requireNonNull(this.player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED)))));

        if (this.polymer$currentlyMinedPos != null) {
            PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, this.polymer$currentlyMinedPos, this.polymer$currentlyMinedState, -1));
            this.polymer$currentlyMinedPos = null;
            this.polymer$currentlyMinedState = null;
        }
    }

    @Redirect(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), require = 0)
    private void polymer$noOneCaresAboutMismatch(Logger instance, String s, Object o, Object o2) {
    }
}