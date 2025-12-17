package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.other.PolymerComponentImpl;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public interface PolymerComponent extends PolymerObject {
    static void registerDataComponent(DataComponentType<?>... types) {
        for (var type : types) {
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.DATA_COMPONENT_TYPE, type);
            PolymerComponentImpl.UNSYNCED_COMPONENTS.add(type);
        }
    }

    static void registerEnchantmentEffectComponent(DataComponentType<?>... types) {
        for (var type : types) {
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, type);
            PolymerComponentImpl.UNSYNCED_COMPONENTS.add(type);
        }
    }

    static boolean isPolymerComponent(DataComponentType<?> type) {
        return PolymerComponentImpl.UNSYNCED_COMPONENTS.contains(type) || type instanceof PolymerObject;
    }

    static boolean canSync(DataComponentType<?> key, @Nullable Object entry, PacketContext context) {
        if (entry instanceof PolymerComponent component && component.canSyncRawToClient(context)) {
            return true;
        } else if (key instanceof PolymerSyncedObject<?> syncedObject && syncedObject.canSyncRawToClient(context)) {
            return true;
        }

        return !isPolymerComponent(key);
    }

    default boolean canSyncRawToClient(PacketContext context) {
        return false;
    }
}
