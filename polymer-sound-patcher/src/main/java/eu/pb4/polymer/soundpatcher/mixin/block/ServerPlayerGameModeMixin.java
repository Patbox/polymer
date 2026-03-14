package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow private int gameTicks;

    @Shadow @Final protected ServerPlayer player;

    @Shadow protected ServerLevel level;

    @Inject(method = "incrementDestroyProgress", at = @At("HEAD"))
    private void polymer$soundMine(BlockState blockState, BlockPos blockPos, int startTime, CallbackInfoReturnable<Float> cir) {
        var destroyTicks = (this.gameTicks - startTime) - 1;
        var group = CoreBridge.getClientSideSoundGroup(blockState, this.player);
        if (SoundRemapperImpl.ignoreExceptions(group.getHitSound()) && destroyTicks % 4 == 0) {
            group = blockState.getSoundType();
            player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(group.getHitSound()),
                    SoundSource.BLOCKS,blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                    (group.getVolume() + 1.0f) / 8.0f, group.getPitch() * 0.5f, level.getRandom().nextLong()));
        }
    }
}