package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import net.minecraft.class_10124;
import net.minecraft.class_10131;
import net.minecraft.class_10132;
import net.minecraft.class_10134;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

@Mixin(class_10132.class)
public abstract class ConsumableComponentMixin implements TransformingComponent {
    @Shadow @Final private List<StatusEffectInstance> effects;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new class_10132(List.of());
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var effect : this.effects) {
            if (effect.getEffectType().value() instanceof PolymerObject) {
                return true;
            }
        }
        return false;
    }
}
