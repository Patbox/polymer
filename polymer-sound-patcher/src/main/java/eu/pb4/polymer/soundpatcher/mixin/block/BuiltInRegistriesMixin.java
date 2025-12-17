package eu.pb4.polymer.soundpatcher.mixin.block;

import eu.pb4.polymer.soundpatcher.api.SoundPatcher;
import eu.pb4.polymer.soundpatcher.impl.SoundPatchImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.SoundType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
    @Inject(method = "freeze", at = @At("TAIL"))
    private static void setupSoundsAsRequested(CallbackInfo ci) {
        if (!SoundPatchImpl.VANILLA_BLOCK_SOUNDS) {
            return;
        }


        for (var field : SoundType.class.getDeclaredFields()) {
            if (field.getType() == SoundType.class) {
                try {
                    SoundPatcher.convertIntoServerSound((SoundType) field.get(null));
                } catch (Throwable e) {
                    // ignored
                }
            }
        }

        for (var block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (id.getNamespace().equals("minecraft")) {
                SoundPatcher.markAsIgnoringSoundExclusions(block.defaultBlockState().getSoundType());
            }
        }
    }
}
