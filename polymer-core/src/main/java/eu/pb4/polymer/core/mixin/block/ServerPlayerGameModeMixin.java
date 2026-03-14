package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
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
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import java.util.Objects;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Final
    @Shadow
    protected ServerPlayer player;
    @Shadow
    protected ServerLevel level;
    @Shadow
    private int destroyProgressStart;
    @Shadow public abstract void destroyAndAck(BlockPos pos, int sequence, String reason);

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

    @Inject(method = "incrementDestroyProgress", at = @At("TAIL"))
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
            this.polymer$currentBreakingProgress += state.getDestroyProgress(this.player, this.player.level(), pos);

            if (this.polymer$currentBreakingProgress >= 1.0F) {
                this.polymer$blockBreakingCooldown = 5;
                this.polymer$currentBreakingProgress = 0;
                this.player.connection.send(new ClientboundBlockDestructionPacket(-1, pos, -1));
                this.destroyAndAck(pos, this.polymer$sequence, "destroyed");
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));
                PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, pos, state, -1));
                this.polymer$currentlyMinedState = null;
                this.polymer$currentlyMinedPos = null;
            } else {
                var k = this.polymer$currentBreakingProgress > 0.0F ? (int)(this.polymer$currentBreakingProgress * 10) : -1;
                this.player.connection.send(new ClientboundBlockDestructionPacket(-1, pos, k));
                polymer$sendMiningFatigue();
                PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, pos, state, k));
            }
        } else if (this.polymer$hasMiningFatigue) {
            this.polymer$clearMiningEffect();
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void polymer_packetReceivedInject(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        this.polymer$sequence = sequence;
        var serverState = this.player.level().getBlockState(pos);
        if (this.polymer$shouldMineServerSide(pos, serverState)) {
            if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                this.polymer$currentBreakingProgress = 0;
                this.polymer$currentlyMinedState = serverState;
                this.polymer$currentlyMinedPos = pos;
                var serverDelta = serverState.getDestroyProgress(this.player, this.level, pos);
                var clientState = serverState;
                if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, serverState.getBlock()) instanceof PolymerBlock virtualBlock) {
                    clientState = PolymerBlockUtils.getBlockStateSafely(virtualBlock, serverState, this.player.connection.getPacketContext());
                }

                float clientDelta = clientState.getDestroyProgress(this.player, this.level, pos);

                if (clientDelta >= 1.0f && serverDelta < 1.0f) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(pos, serverState));
                }
                PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, pos, serverState, 0));

                if (serverDelta < 1.0f) {
                    polymer$sendMiningFatigue();
                }
            } else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                if (this.polymer$hasMiningFatigue) {
                    this.polymer$clearMiningEffect();
                }
                this.player.connection.send(new ClientboundBlockDestructionPacket(-1, pos, -1));
            }
        } else if (this.polymer$hasMiningFatigue) {
            this.polymer$clearMiningEffect();
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", ordinal = 0))
    private void polymer$clearBreakingTime(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        this.polymer$currentBreakingProgress = 0;
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyAndAck(Lnet/minecraft/core/BlockPos;ILjava/lang/String;)V", ordinal = 1))
    private void polymer$clearBreakingTimeInstaMine(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        this.polymer$currentBreakingProgress = 0;
    }

    @Inject(method = "handleBlockBreakAction", at = @At("TAIL"))
    private void polymer$enforceBlockBreakingCooldown(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (this.polymer$shouldMineServerSide(pos, this.player.level().getBlockState(pos))) {
            if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                this.destroyProgressStart += polymer$blockBreakingCooldown;
            }
        } else if (this.polymer$hasMiningFatigue) {
            this.polymer$clearMiningEffect();
        }
    }

    @Inject(method = "destroyAndAck", at = @At("HEAD"))
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
        var x = new AttributeInstance(Attributes.BLOCK_BREAK_SPEED, (a) -> {});
        x.setBaseValue(-9999);
        this.player.connection.send(new ClientboundUpdateAttributesPacket(this.player.getId(), List.of(x)));
    }

    @Unique
    private void polymer$clearMiningEffect() {
        this.polymer$hasMiningFatigue = false;
        this.player.connection.send(new ClientboundUpdateAttributesPacket(this.player.getId(),
                List.of(Objects.requireNonNull(this.player.getAttribute(Attributes.BLOCK_BREAK_SPEED)))));

        if (this.polymer$currentlyMinedPos != null) {
            PolymerBlockUtils.BREAKING_PROGRESS_UPDATE.invoke(x -> x.onBreakingProgressUpdate(player, this.polymer$currentlyMinedPos, this.polymer$currentlyMinedState, -1));
            this.polymer$currentlyMinedPos = null;
            this.polymer$currentlyMinedState = null;
        }
    }

    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), require = 0)
    private void polymer$noOneCaresAboutMismatch(Logger instance, String s, Object o, Object o2) {
    }
}