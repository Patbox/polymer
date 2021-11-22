package eu.pb4.polymer.api.other;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class PolymerStat extends Identifier implements PolymerObject {

    private PolymerStat(String id) {
        super(id);
    }

    /**
     * Register a custom server-compatible statistic.
     * Registering a {@link net.minecraft.stat.Stat} in the vanilla way will cause clients to disconnect when opening the statistics screen.
     * @param id the Identifier for the stat
     * @param formatter the formatter for the stat to use
     * @return the PolymerStat ({@link Identifier}) for the custom stat
     */
    public static Identifier registerStat(String id, StatFormatter formatter) {
        PolymerStat identifier = new PolymerStat(id);
        Registry.register(Registry.CUSTOM_STAT, id, identifier);
        Stats.CUSTOM.getOrCreateStat(identifier, formatter);
        return identifier;
    }
}
