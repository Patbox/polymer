package eu.pb4.polymer.mixin.client.entity;

import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientEntityExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceName(CallbackInfoReturnable<Text> cir) {
        var id = ((ClientEntityExtension) this).polymer_getId();
        if (id != null) {
            var type = InternalClientRegistry.ENTITY_TYPE.get(id);

            if (type != null) {
                cir.setReturnValue(type.name());
            }
        }
    }
}
