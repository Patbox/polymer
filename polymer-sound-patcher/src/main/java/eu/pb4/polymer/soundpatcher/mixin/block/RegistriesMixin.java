package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.api.SoundPatcher;
import eu.pb4.polymer.soundpatcher.impl.SoundPatchImpl;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registries.class)
public class RegistriesMixin {
    @Inject(method = "freezeRegistries", at = @At("TAIL"))
    private static void setupSoundsAsRequested(CallbackInfo ci) {
        if (!SoundPatchImpl.VANILLA_BLOCK_SOUNDS) {
            return;
        }


        for (var field : BlockSoundGroup.class.getDeclaredFields()) {
            if (field.getType() == BlockSoundGroup.class) {
                try {
                    SoundPatcher.convertIntoServerSound((BlockSoundGroup) field.get(null));
                } catch (Throwable e) {
                    // ignored
                }
            }
        }

        for (var block : Registries.BLOCK) {
            var id = Registries.BLOCK.getId(block);
            if (id.getNamespace().equals("minecraft")) {
                SoundPatcher.markAsIgnoringSoundExclusions(block.getDefaultState().getSoundGroup());
            }
        }
    }
}
