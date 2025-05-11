package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "spawnBreakParticles", at = @At("TAIL"))
    private void polymer$spawnBreakParticles(World world, PlayerEntity player, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        if (world.isClient()) {
            return;
        }
        BlockSoundGroup group;
        group = CoreBridge.getClientSideSoundGroupBreaking(blockState, PacketContext.create(world.getRegistryManager()));
        if (group.getBreakSound() != null && SoundRemapperImpl.ignoreExceptions(group.getBreakSound())) {
            group = blockState.getSoundGroup();
            world.playSound(null, blockPos, group.getBreakSound(), SoundCategory.BLOCKS, (group.getVolume() + 1.0f) / 2.0f, group.getPitch() * 0.8f);
        }
    }
}