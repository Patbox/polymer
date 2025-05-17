package eu.pb4.polymer.soundpatcher.impl;

import eu.pb4.polymer.common.api.ScopedOverride;
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

    public static boolean register(Identifier from, Identifier to) {
        enable();
        if (REMAPPED_SOUND_IDS.containsKey(from) || ignored) {
            return false;
        }
        REMAPPED_SOUND_IDS.put(from, to);
        SOUND_EXCEPTION_IGNORER.add(from);
        return true;
    }

    public static void enable() {
        ignored = SoundPatchImpl.FORCE_DISABLE;
    }

    public static ScopedOverride ignorePlaySoundExclusion() {
        enable();
        if (SoundRemapperImpl.IGNORE_PLAY_SOUND_EXCLUSION.get() != null || ignored) {
            return ScopedOverride.NO_OP;
        }
        SoundRemapperImpl.IGNORE_PLAY_SOUND_EXCLUSION.set(Unit.INSTANCE);
        return SoundRemapperImpl.IGNORE_PLAY_SOUND_EXCLUSION::remove;
    }

    public static boolean ignoreExceptions(SoundEvent value) {
        if (ignored) {
            return false;
        }

        if (IGNORE_PLAY_SOUND_EXCLUSION.get() != null) {
            return true;
        }

        return SOUND_EXCEPTION_IGNORER.contains(value.id());
    }
}
