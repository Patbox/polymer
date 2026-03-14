package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.other.PolymerConsumeEffect;
import eu.pb4.polymer.core.impl.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

@Mixin(Consumable.class)
public abstract class ConsumableMixin implements TransformingComponent {

    @Shadow @Final private float consumeSeconds;

    @Shadow @Final private Holder<SoundEvent> sound;

    @Shadow @Final private boolean hasConsumeParticles;

    @Shadow @Final private List<ConsumeEffect> onConsumeEffects;

    @Shadow @Final private ItemUseAnimation animation;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new Consumable(this.consumeSeconds, this.animation, this.sound, this.hasConsumeParticles, List.of());
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var effect : this.onConsumeEffects) {
            if (effect instanceof TransformingComponent t && t.polymer$requireModification(context) || !PolymerConsumeEffect.canSync(effect.getType(), effect, context)) {
                return true;
            }
        }
        return false;
    }
}
