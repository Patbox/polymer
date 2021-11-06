package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.impl.entity.PolymerTrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TrackedDataHandlerRegistry.class)
public class TrackedDataHandlerRegistryMixin {
    @ModifyVariable(method = "getId", at = @At("HEAD"))
    private static TrackedDataHandler<?> polymer_replaceWithReal(TrackedDataHandler<?> x) {
        return x instanceof PolymerTrackedDataHandler y ? y.getReal() : x;
    }
}
