package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.api.SoundPatcher;
import eu.pb4.polymer.soundpatcher.impl.SoundPatchImpl;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
            var id = Registries.BLOCK.getId(block);
            var group = block.getDefaultState().getSoundGroup();
            if (group != null) {
                handleSound(id, group.getHitSound());
                handleSound(id, group.getBreakSound());
                handleSound(id, group.getFallSound());
                handleSound(id, group.getPlaceSound());
                handleSound(id, group.getStepSound());
            }
        }
    }

    @Unique
    private static void handleSound(Identifier id, SoundEvent event) {
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && event.id().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && SoundPatchImpl.VANILLA_BLOCK_SOUNDS) {
            SoundPatcher.convertIntoServerSound(event);
        } else if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && SoundPatchImpl.MODDED_BLOCK_SOUNDS) {
            SoundPatcher.markAsIgnoringSoundExclusions(event);
        }
    }
}
