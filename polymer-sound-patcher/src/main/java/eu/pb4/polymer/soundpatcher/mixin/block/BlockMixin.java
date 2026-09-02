package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "spawnDestroyByEntityParticles", at = @At("TAIL"))
    private void polymer$spawnBreakParticles(Level level, Entity entity, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (level.isClientSide()) {
            return;
        }
        SoundType group = state.getSoundType();
        if (SoundRemapperImpl.ignoreExceptions(group.getBreakSound())) {
            group = state.getSoundType();
            level.playSound(null, pos, group.getBreakSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0f) / 2.0f, group.getPitch() * 0.8f);
        }
    }
}