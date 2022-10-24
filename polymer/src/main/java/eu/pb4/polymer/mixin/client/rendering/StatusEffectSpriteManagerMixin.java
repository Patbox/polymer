package eu.pb4.polymer.mixin.client.rendering;

import net.minecraft.client.texture.StatusEffectSpriteManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StatusEffectSpriteManager.class)
public class StatusEffectSpriteManagerMixin {
    /*@Inject(method = "getSprites", at = @At("RETURN"), cancellable = true)
    private void polymer_skipPolymerEntries(CallbackInfoReturnable<Stream<Identifier>> cir) {
        cir.setReturnValue(cir.getReturnValue().filter((id) -> PolymerKeepModel.useClientModel(Registry.STATUS_EFFECT.get(id))));
    }*/
}
