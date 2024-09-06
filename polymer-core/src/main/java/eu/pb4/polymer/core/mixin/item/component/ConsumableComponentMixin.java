package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.impl.TransformingComponent;
import net.minecraft.component.type.Consumable;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

@Mixin(ConsumableComponent.class)
public abstract class ConsumableComponentMixin implements TransformingComponent {

    @Shadow @Final private float consumeSeconds;

    @Shadow @Final private RegistryEntry<SoundEvent> sound;

    @Shadow @Final private boolean hasConsumeParticles;

    @Shadow @Final private List<ConsumeEffect> onConsumeEffects;

    @Shadow @Final private UseAction useAction;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new ConsumableComponent(this.consumeSeconds, this.useAction, this.sound, this.hasConsumeParticles, List.of());
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
