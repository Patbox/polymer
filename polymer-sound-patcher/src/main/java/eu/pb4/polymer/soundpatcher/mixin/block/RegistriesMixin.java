package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.api.SoundPatcher;
import eu.pb4.polymer.soundpatcher.impl.SoundPatchImpl;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registries.class)
public class RegistriesMixin {
    @Inject(method = "freezeRegistries", at = @At("TAIL"))
    private static void setupSoundsAsRequested(CallbackInfo ci) {
        if (!SoundPatchImpl.VANILLA_BLOCK_SOUNDS && !SoundPatchImpl.MODDED_BLOCK_SOUNDS) {
            return;
        }

        for (var block : Registries.BLOCK) {
            var group = block.getDefaultState().getSoundGroup();
            if (group != null) {
                handleSound(group.getHitSound());
                handleSound(group.getBreakSound());
                handleSound(group.getFallSound());
                handleSound(group.getPlaceSound());
                handleSound(group.getStepSound());
            }
        }
    }

    private static void handleSound(SoundEvent event) {
        if (event.id().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && SoundPatchImpl.VANILLA_BLOCK_SOUNDS) {
            SoundPatcher.convertIntoServerSound(event);
        } else if (!event.id().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && SoundPatchImpl.MODDED_BLOCK_SOUNDS) {
            SoundPatcher.markAsIgnoringSoundExclusions(event);
        }
    }
}
