package eu.pb4.polymer.mixin.client.entity;

import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientEntityExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixin implements ClientEntityExtension {
    @Shadow public @Nullable abstract Text getCustomName();

    @Unique private Identifier polymer_client_entityId = null;

    @Override
    public void polymer_setId(Identifier id) {
        this.polymer_client_entityId = id;
    }

    @Override
    public Identifier polymer_getId() {
        return this.polymer_client_entityId;
    }

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceName(CallbackInfoReturnable<Text> cir) {
        if (this.polymer_client_entityId != null && this.getCustomName() == null) {
            var type = InternalClientRegistry.ENTITY_TYPE.get(this.polymer_client_entityId);

            if (type != null) {
                cir.setReturnValue(type.name());
            }
        }
    }
}
