package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow private int tickCounter;

    @Shadow @Final protected ServerPlayerEntity player;

    @Shadow protected ServerWorld world;

    @Inject(method = "continueMining", at = @At("HEAD"))
    private void polymer$soundMine(BlockState blockState, BlockPos blockPos, int startTime, CallbackInfoReturnable<Float> cir) {
        var destroyTicks = (this.tickCounter - startTime) - 1;
        var group = CoreBridge.getClientSideSoundGroup(blockState, PacketContext.create(this.player));
        if (SoundRemapperImpl.ignoreExceptions(group.getHitSound()) && destroyTicks % 4 == 0) {
            group = blockState.getSoundGroup();
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(group.getHitSound()),
                    SoundCategory.BLOCKS,blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                    (group.getVolume() + 1.0f) / 8.0f, group.getPitch() * 0.5f, world.getRandom().nextLong()));
        }
    }
}