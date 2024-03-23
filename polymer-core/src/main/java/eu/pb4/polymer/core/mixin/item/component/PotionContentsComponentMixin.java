package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.TransformingDataComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;

@Mixin(PotionContentsComponent.class)
public abstract class PotionContentsComponentMixin implements TransformingDataComponent {
    @Shadow @Final private Optional<RegistryEntry<Potion>> potion;

    @Shadow @Final private List<StatusEffectInstance> customEffects;

    @Shadow public abstract int getColor();

    @Override
    public Object polymer$getTransformed(ServerPlayerEntity player) {
        if (!polymer$requireModification(player)) {
            return this;
        }

        return new PotionContentsComponent(Optional.empty(), Optional.of(this.getColor()), List.of());
    }

    @Override
    public boolean polymer$requireModification(ServerPlayerEntity player) {
        if (this.potion.isPresent() && this.potion.get() instanceof PolymerObject) {
            return true;
        }

        for (StatusEffectInstance statusEffectInstance : this.customEffects) {
            if (statusEffectInstance.getEffectType() instanceof PolymerObject) {
                return true;
            }
        }
        return false;
    }
}
