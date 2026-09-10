package eu.pb4.polymer.soundpatcher.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow @Final protected ServerPlayer player;

    @Shadow protected ServerLevel level;

    @Shadow
    private BlockPos destroyPos;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V"))
    private void polymer$soundMine(CallbackInfo ci, @Local(name = "event") int event, @Local(name = "blockState") BlockState blockState) {
        if (event != LevelEvent.PARTICLES_AND_SOUND_DESTROY_PROGRESS) {
            return;
        }

        var group = CoreBridge.getClientSideSoundGroup(blockState, this.player);
        if (SoundRemapperImpl.ignoreExceptions(group.getHitSound())) {
            group = blockState.getSoundType();
            this.level.playSound(null, this.destroyPos, group.getHitSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0f) / 8.0f, group.getPitch() * 0.5f);
        }
    }
}