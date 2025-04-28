package eu.pb4.polymer.blocks.mixin.sound;

import eu.pb4.polymer.blocks.api.PolymerSoundBlock;
import eu.pb4.polymer.blocks.impl.PolymerBlockSounds;
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

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "spawnBreakParticles", at = @At("TAIL"))
    private void polymer$spawnBreakParticles(World world, PlayerEntity player, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        BlockSoundGroup soundCategory = null;
        if (blockState.getBlock() instanceof PolymerSoundBlock) {
            soundCategory = blockState.getSoundGroup();
        }
        else if (PolymerBlockSounds.REMIXES.containsValue(blockState.getSoundGroup())) {
            soundCategory = blockState.getSoundGroup();
        }

        if (soundCategory != null)
            world.playSound(null, blockPos, soundCategory.getBreakSound(), SoundCategory.BLOCKS, (soundCategory.getVolume() + 1.0f) / 2.0f, soundCategory.getPitch() * 0.8f);
    }
}
