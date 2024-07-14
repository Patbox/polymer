package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.other.PolymerComponentImpl;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public interface PolymerComponent extends PolymerObject {
    static void registerDataComponent(ComponentType<?>... types) {
        for (var type : types) {
            RegistrySyncUtils.setServerEntry(Registries.DATA_COMPONENT_TYPE, type);
            PolymerComponentImpl.UNSYNCED_COMPONENTS.add(type);
        }
    }

    static void registerEnchantmentEffectComponent(ComponentType<?>... types) {
        for (var type : types) {
            RegistrySyncUtils.setServerEntry(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, type);
            PolymerComponentImpl.UNSYNCED_COMPONENTS.add(type);
        }
    }

    static boolean isPolymerComponent(ComponentType<?> type) {
        return PolymerComponentImpl.UNSYNCED_COMPONENTS.contains(type);
    }

    static boolean canSync(ComponentType<?> key, @Nullable Object entry, PacketContext context) {
        if (entry instanceof PolymerComponent component && component.canSyncRawToClient(context)) {
            return true;
        }

        return !isPolymerComponent(key);
    }

    default boolean canSyncRawToClient(PacketContext context) {
        return false;
    }
}
