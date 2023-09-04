package eu.pb4.polymer.rsm.impl.forge;

import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ForgeRegistryUtils {
    private static final Field IDS_FIELD;

    static {
        try {
            IDS_FIELD = Class.forName("net.minecraftforge.registries.ForgeRegistry$Snapshot").getField("ids");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static Object clearPolymerEntries(Object o) {
        var entry = (Map.Entry<Identifier, Object>) o;
        var registry = Registries.REGISTRIES.get(entry.getKey());
        if (registry == null) {
            return o;
        }
        try {
            var map = (Map<Identifier, Integer>) IDS_FIELD.get(entry.getValue());

            for (var id : List.copyOf(map.keySet())) {
                if (RegistrySyncUtils.isServerEntry(registry, id)) {
                    map.remove(id);
                }
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return o;
    }
}
