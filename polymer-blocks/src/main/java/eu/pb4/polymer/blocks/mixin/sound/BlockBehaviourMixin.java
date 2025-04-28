package eu.pb4.polymer.blocks.mixin.sound;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.blocks.impl.PolymerBlockSounds;
import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBlock.class)
public class BlockBehaviourMixin {
    @ModifyReturnValue(method = "getSoundGroup", at = @At("RETURN"))
    private BlockSoundGroup filament$modifySoundType(BlockSoundGroup original) {
        var remix = PolymerBlockSounds.REMIXES.get(original);
        if (remix != null)
            return remix;

        return original;
    }
}
