package eu.pb4.polymer.core.mixin.client.entity;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientEntityExtension;
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

    @Unique private Identifier polymer$entityId = null;

    @Override
    public void polymer$setId(Identifier id) {
        this.polymer$entityId = id;
    }

    @Override
    public Identifier polymer$getId() {
        return this.polymer$entityId;
    }

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceName(CallbackInfoReturnable<Text> cir) {
        if (this.polymer$entityId != null && this.getCustomName() == null) {
            var type = InternalClientRegistry.ENTITY_TYPES.get(this.polymer$entityId);

            if (type != null) {
                cir.setReturnValue(type.name());
            }
        }
    }
}
