package eu.pb4.polymer.core.mixin.item;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemEnchantmentsComponent.class)
public interface ItemEnchantmentsComponentAccessor {
    @Invoker("<init>")
    static ItemEnchantmentsComponent create(Object2IntLinkedOpenHashMap<RegistryEntry<Enchantment>> enchantments, boolean showInTooltip) {
        throw new UnsupportedOperationException();
    }
}
