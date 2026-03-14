package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.other.PolymerComponentImpl;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public interface PolymerConsumeEffect extends PolymerObject {
    static void registerConsumeEffect(ConsumeEffect.Type<?>... types) {
        for (var type : types) {
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.CONSUME_EFFECT_TYPE, type);
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
