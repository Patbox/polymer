package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.SuspiciousStewEffects;

@Mixin(SuspiciousStewEffects.class)
public abstract class SuspiciousStewEffectsMixin implements TransformingComponent {

    @Shadow @Final private List<SuspiciousStewEffects.Entry> effects;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new SuspiciousStewEffects(List.of());
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var effect : this.effects) {
            if (!PolymerSyncedObject.canSyncRawToClient(BuiltInRegistries.MOB_EFFECT, effect.effect().value(), context)) {
                return true;
            }
        }
        return false;
    }
}
