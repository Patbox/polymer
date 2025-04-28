package eu.pb4.polymer.blocks.mixin.sound;

import eu.pb4.polymer.blocks.api.PolymerSoundBlock;
import eu.pb4.polymer.blocks.impl.PolymerBlockSounds;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// handle mining sounds
@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerGameModeMixin {
    @Shadow protected ServerWorld world;

    @Shadow private int tickCounter;

    @Inject(method = "continueMining", at = @At("HEAD"))
    private void filament$soundMine(BlockState blockState, BlockPos blockPos, int startTime, CallbackInfoReturnable<Float> cir) {
        var destroyTicks = (tickCounter - startTime) - 1;
        if ((blockState.getBlock() instanceof PolymerSoundBlock || PolymerBlockSounds.REMIXES.containsValue(blockState.getSoundGroup())) && destroyTicks % 4 == 0) {
            BlockSoundGroup soundType = blockState.getSoundGroup();
            world.playSound(null, blockPos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 8.0f, soundType.getPitch() * 0.5f);
        }
    }
}
