package eu.pb4.polymer.soundpatcher.impl;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoundRemapperImpl {
    public static final ThreadLocal<Unit> IGNORE_PLAY_SOUND_EXCLUSION = new ThreadLocal<>();

    private static boolean ignored = true;
    private static final Map<Identifier, Identifier> REMAPPED_SOUND_IDS = new HashMap<>();
    public static final Set<Identifier> SOUND_EXCEPTION_IGNORER = new HashSet<>();

    public static SoundEvent remap(SoundEvent event) {
        if (ignored) {
            return event;
        }

        var id = REMAPPED_SOUND_IDS.get(event.id());
        if (id == null) {
            return event;
        }

        return new SoundEvent(id, event.fixedRange());
    }

    public static void register(SoundEvent from, Identifier to) {
        register(from.id(), to);
    }

    public static void register(Identifier from, Identifier to) {
        enable();
        REMAPPED_SOUND_IDS.put(from, to);
        SOUND_EXCEPTION_IGNORER.add(from);
    }

    public static void enable() {
        ignored = false;
    }

    public static boolean ignoreExceptions(SoundEvent value) {
        if (IGNORE_PLAY_SOUND_EXCLUSION.get() != null) {
            return true;
        }

        return SOUND_EXCEPTION_IGNORER.contains(value.id());
    }
}
