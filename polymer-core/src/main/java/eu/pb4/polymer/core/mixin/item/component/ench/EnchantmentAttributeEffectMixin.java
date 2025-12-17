package eu.pb4.polymer.core.mixin.item.component.ench;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.PolymericObject;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnchantmentAttributeEffect.class)
public class EnchantmentAttributeEffectMixin implements PolymericObject {
    @Shadow @Final private Holder<Attribute> attribute;

    @Override
    public boolean polymer$isPolymeric() {
        return PolymerEntityUtils.isPolymerEntityAttribute(this.attribute);
    }
}
