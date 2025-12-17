package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.MenuType;
import java.util.Set;

public final class PolymerScreenHandlerUtils {
    private static final Set<MenuType<?>> POLYMER_TYPES = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);

    private PolymerScreenHandlerUtils() {}

    public static void registerType(MenuType<?>... types) {
        for (var type : types) {
            POLYMER_TYPES.add(type);
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.MENU, type);
        }
    }

    public static boolean isPolymerType(MenuType<?> type) {
        return POLYMER_TYPES.contains(type);
    }
}
