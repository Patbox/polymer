package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.other.PolymerComponentImpl;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public interface PolymerConsumeEffect extends PolymerObject {
    static void registerConsumeEffect(ConsumeEffect.Type<?>... types) {
        for (var type : types) {
            RegistrySyncUtils.setServerEntry(Registries.CONSUME_EFFECT_TYPE, type);
            PolymerComponentImpl.UNSYNCED_CONSUME_EFFECTS.add(type);
        }
    }

    static boolean canSync(ConsumeEffect.Type<?> key, @Nullable ConsumeEffect entry, PacketContext context) {
        if (entry instanceof PolymerConsumeEffect component && component.canSyncRawToClient(context)) {
            return true;
        }

        return !PolymerComponentImpl.UNSYNCED_CONSUME_EFFECTS.contains(key);
    }

    default boolean canSyncRawToClient(PacketContext context) {
        return false;
    }
}
