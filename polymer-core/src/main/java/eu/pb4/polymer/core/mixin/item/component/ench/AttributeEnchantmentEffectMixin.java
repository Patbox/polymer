package eu.pb4.polymer.core.mixin.item.component.ench;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.PolymericObject;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AttributeEnchantmentEffect.class)
public class AttributeEnchantmentEffectMixin implements PolymericObject {
    @Shadow @Final private RegistryEntry<EntityAttribute> attribute;

    @Override
    public boolean polymer$isPolymeric() {
        return PolymerEntityUtils.isPolymerEntityAttribute(this.attribute);
    }
}
