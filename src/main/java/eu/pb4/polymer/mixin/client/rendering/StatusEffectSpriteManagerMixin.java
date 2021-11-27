package eu.pb4.polymer.mixin.client.rendering;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(StatusEffectSpriteManager.class)
public class StatusEffectSpriteManagerMixin {
    @Inject(method = "getSprites", at = @At("RETURN"), cancellable = true)
    private void polymer_skipPolymerEntries(CallbackInfoReturnable<Stream<Identifier>> cir) {
        cir.setReturnValue(cir.getReturnValue().filter((id) -> !PolymerObject.is(Registry.STATUS_EFFECT.get(id))));
    }
}
