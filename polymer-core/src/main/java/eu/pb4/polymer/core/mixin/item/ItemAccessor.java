package eu.pb4.polymer.core.mixin.item;

import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.item.Item.class)
public interface ItemAccessor {
    @Mutable
    @Accessor
    void setRequiredFeatures(FeatureFlagSet requiredFeatures);
}
