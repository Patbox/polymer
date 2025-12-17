package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public final class PolymerStat {
    private static final Map<Identifier, Component> NAMES = new HashMap<>();

    /**
     * Register a custom server-compatible statistic.
     * Registering a {@link net.minecraft.stats.Stat} in the vanilla way will cause clients to disconnect when opening the statistics screen.
     *
     * @param id        the Identifier for the stat
     * @param formatter the formatter for the stat to use
     * @return the PolymerStat ({@link Identifier}) for the custom stat
     */
    public static Identifier registerStat(String id, StatFormatter formatter) {
        return registerStat(id, Component.translatable("stat." + id.replace(':', '.')), formatter);
    }

    /**
     * Register a custom server-compatible statistic.
     * Registering a {@link net.minecraft.stats.Stat} in the vanilla way will cause clients to disconnect when opening the statistics screen.
     *
     * @param id        the Identifier for the stat
     * @param name      the name used in /polymer stats
     * @param formatter the formatter for the stat to use
     * @return the PolymerStat ({@link Identifier}) for the custom stat
     */
    public static Identifier registerStat(String id, Component name, StatFormatter formatter) {
        var idx = Identifier.parse(id);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, idx, idx);
        Stats.CUSTOM.get(idx, formatter);
        //noinspection unchecked
        RegistrySyncUtils.setServerEntry((Registry<Object>) (Object) BuiltInRegistries.CUSTOM_STAT, (Object) idx);
        NAMES.put(idx, name);
        return idx;
    }

    /**
     * Register a custom server-compatible statistic.
     * Registering a {@link net.minecraft.stats.Stat} in the vanilla way will cause clients to disconnect when opening the statistics screen.
     *
     * @param id        the Identifier for the stat
     * @param formatter the formatter for the stat to use
     * @return the PolymerStat ({@link Identifier}) for the custom stat
     */
    public static Identifier registerStat(Identifier id, StatFormatter formatter) {
        return registerStat(id.toString(), formatter);
    }

    /**
     * Register a custom server-compatible statistic.
     * Registering a {@link net.minecraft.stats.Stat} in the vanilla way will cause clients to disconnect when opening the statistics screen.
     *
     * @param id        the Identifier for the stat
     * @param name      the name used in /polymer stats
     * @param formatter the formatter for the stat to use
     * @return the PolymerStat ({@link Identifier}) for the custom stat
     */
    public static Identifier registerStat(Identifier id, Component name, StatFormatter formatter) {
        return registerStat(id.toString(), name, formatter);
    }


    public static Component getName(Identifier identifier) {
        return NAMES.getOrDefault(identifier, Component.empty());
    }
}
