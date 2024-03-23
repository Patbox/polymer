package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.TransformingDataComponent;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ItemEnchantmentsComponent.class)
public abstract class ItemEnchantmentComponentMixin implements TransformingDataComponent {
    @Shadow public abstract boolean isEmpty();

    @Shadow public abstract Set<RegistryEntry<Enchantment>> getEnchantments();

    @Shadow public abstract Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> getEnchantmentsMap();

    @Override
    public Object polymer$getTransformed(ServerPlayerEntity player) {
        if (!polymer$requireModification(player)) {
            return this;
        }
        var b = new ItemEnchantmentsComponent.Builder((ItemEnchantmentsComponent) (Object) this);
        b.remove(x -> x.value() instanceof PolymerObject);

        for (var e : this.getEnchantmentsMap()) {
            if (e.getKey().value() instanceof PolymerSyncedObject polyEnch) {
                var possible = (Enchantment) polyEnch.getPolymerReplacement(player);

                if (possible != null) {
                    b.set(possible, e.getIntValue());
                }
            }
        }
        return b.build();
    }

    @Override
    public boolean polymer$requireModification(ServerPlayerEntity player) {
        if (!this.isEmpty()) {
            for (var ench : this.getEnchantments()) {
                if (ench.value() instanceof PolymerObject) {
                    if (ench.value() instanceof PolymerSyncedObject polymerEnchantment && polymerEnchantment.getPolymerReplacement(player) == ench.value()) {
                        continue;
                    }

                    return true;
                }
            }
        }
        return false;
    }
}
