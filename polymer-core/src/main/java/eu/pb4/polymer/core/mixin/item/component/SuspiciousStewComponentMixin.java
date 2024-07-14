package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

@Mixin(SuspiciousStewEffectsComponent.class)
public abstract class SuspiciousStewComponentMixin implements TransformingComponent {

    @Shadow @Final private List<SuspiciousStewEffectsComponent.StewEffect> effects;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new SuspiciousStewEffectsComponent(List.of());
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var effect : this.effects) {
            if (effect.effect().value() instanceof PolymerObject) {
                return true;
            }
        }
        return false;
    }
}
