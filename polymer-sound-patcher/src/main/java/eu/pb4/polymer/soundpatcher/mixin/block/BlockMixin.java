package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "spawnDestroyParticles", at = @At("TAIL"))
    private void polymer$spawnBreakParticles(Level world, Player player, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        if (world.isClientSide()) {
            return;
        }
        SoundType group;
        group = CoreBridge.getClientSideSoundGroupBreaking(blockState, PacketContext.create(world.registryAccess()));
        if (group.getBreakSound() != null && SoundRemapperImpl.ignoreExceptions(group.getBreakSound())) {
            group = blockState.getSoundType();
            world.playSound(null, blockPos, group.getBreakSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0f) / 2.0f, group.getPitch() * 0.8f);
        }
    }
}