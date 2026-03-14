package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

@Mixin(PotionContents.class)
public abstract class PotionContentsMixin implements TransformingComponent {

    @Shadow @Final private List<MobEffectInstance> customEffects;

    @Shadow public abstract int getColor();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Shadow @Final private Optional<Holder<Potion>> potion;

    @Shadow @Final private Optional<String> customName;

    @Shadow public abstract Optional<Holder<Potion>> potion();

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new PotionContents(Optional.empty(), Optional.of(this.getColor()), List.of(),
                this.customName.or(() -> this.potion().map(Holder::value).map(Potion::name)));
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        if (this.potion.isPresent() && !PolymerSyncedObject.canSyncRawToClient(BuiltInRegistries.POTION, this.potion.get().value(), context)) {
            return true;
        }

        for (MobEffectInstance effect : this.customEffects) {
            if (!PolymerSyncedObject.canSyncRawToClient(BuiltInRegistries.MOB_EFFECT, effect.getEffect().value(), context)) {
                return true;
            }
        }
        return false;
    }
}
