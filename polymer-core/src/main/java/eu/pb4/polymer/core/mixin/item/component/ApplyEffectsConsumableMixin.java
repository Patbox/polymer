package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.impl.TransformingComponent;
import net.minecraft.class_10124;
import net.minecraft.class_10134;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

@Mixin(class_10124.class)
public abstract class ApplyEffectsConsumableMixin implements TransformingComponent {

    @Shadow @Final private float consumeSeconds;

    @Shadow @Final private UseAction animation;

    @Shadow @Final private RegistryEntry<SoundEvent> sound;

    @Shadow @Final private boolean hasConsumeParticles;

    @Shadow @Final private List<class_10134> onConsumeEffects;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new class_10124(this.consumeSeconds, this.animation, this.sound, this.hasConsumeParticles, List.of());
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var effect : this.onConsumeEffects) {
            if (effect instanceof TransformingComponent t && t.polymer$requireModification(context)) {
                return true;
            }
        }
        return false;
    }
}
