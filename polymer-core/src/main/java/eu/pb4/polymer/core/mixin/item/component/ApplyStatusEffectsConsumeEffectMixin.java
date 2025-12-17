package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

@Mixin(ApplyStatusEffectsConsumeEffect.class)
public abstract class ApplyStatusEffectsConsumeEffectMixin implements TransformingComponent {
    @Shadow @Final private List<MobEffectInstance> effects;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new ApplyStatusEffectsConsumeEffect(List.of());
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var effect : this.effects) {
            if (!PolymerSyncedObject.canSyncRawToClient(BuiltInRegistries.MOB_EFFECT, effect.getEffect().value(), context)) {
                return true;
            }
        }
        return false;
    }
}
