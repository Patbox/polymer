package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Util;

import java.util.Set;

public final class PolymerScreenHandlerUtils {
    private static final Set<ScreenHandlerType<?>> POLYMER_TYPES = new ObjectOpenCustomHashSet<>(Util.identityHashStrategy());

    private PolymerScreenHandlerUtils() {}

    public static void registerType(ScreenHandlerType<?>... types) {
        for (var type : types) {
            POLYMER_TYPES.add(type);
            RegistrySyncUtils.setServerEntry(Registries.SCREEN_HANDLER, type);
        }
    }

    public static boolean isPolymerType(ScreenHandlerType<?> type) {
        return POLYMER_TYPES.contains(type);
    }
}
