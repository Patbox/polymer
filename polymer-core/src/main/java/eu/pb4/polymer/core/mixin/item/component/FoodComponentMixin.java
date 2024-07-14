package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

@Mixin(FoodComponent.class)
public abstract class FoodComponentMixin implements TransformingComponent {
    @Shadow @Final private int nutrition;

    @Shadow @Final private boolean canAlwaysEat;

    @Shadow @Final private float eatSeconds;

    @Shadow @Final private List<FoodComponent.StatusEffectEntry> effects;

    @Shadow @Final private float saturation;

    @Shadow public abstract Optional<ItemStack> usingConvertsTo();

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        return new FoodComponent(this.nutrition, this.saturation, this.canAlwaysEat, this.eatSeconds, this.usingConvertsTo(), List.of());
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var effect : this.effects) {
            if (effect.effect().getEffectType().value() instanceof PolymerObject) {
                return true;
            }
        }
        return false;
    }
}
