package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

@Mixin(PotionContentsComponent.class)
public abstract class PotionContentsComponentMixin implements TransformingComponent {

    @Shadow @Final private List<StatusEffectInstance> customEffects;

    @Shadow public abstract int getColor();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Shadow @Final private Optional<RegistryEntry<Potion>> potion;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new PotionContentsComponent(Optional.empty(), Optional.of(this.getColor()), List.of(), Optional.empty());
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        if (this.potion.isPresent() && this.potion.get().value() instanceof PolymerObject) {
            return true;
        }

        for (StatusEffectInstance statusEffectInstance : this.customEffects) {
            if (statusEffectInstance.getEffectType().value() instanceof PolymerObject) {
                return true;
            }
        }
        return false;
    }
}
