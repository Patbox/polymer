package eu.pb4.polymer.soundpatcher.api;

import eu.pb4.polymer.common.api.ScopedOverride;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import eu.pb4.polymer.soundpatcher.impl.SoundResourceGenerator;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

public final class SoundPatcher {
    private SoundPatcher(){}

    public static void markAsIgnoringSoundExceptions(SoundEvent soundEvent) {
        markAsIgnoringSoundExceptions(soundEvent.id());
    }

    public static void markAsIgnoringSoundExceptions(Identifier id) {
        SoundRemapperImpl.SOUND_EXCEPTION_IGNORER.add(id);
    }

    public static void convertIntoServerSound(SoundEvent soundEvent) {
        convertIntoServerSound(soundEvent.id());
    }

    public static void convertIntoServerSound(BlockSoundGroup soundGroup) {
        convertIntoServerSound(soundGroup.getStepSound());
        convertIntoServerSound(soundGroup.getBreakSound());
        convertIntoServerSound(soundGroup.getFallSound());
        convertIntoServerSound(soundGroup.getHitSound());
        convertIntoServerSound(soundGroup.getPlaceSound());
    }

    public static void convertIntoServerSound(Identifier soundEvent) {
        if (!soundEvent.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            throw new IllegalArgumentException("Only vanilla / minecraft sound events are supported, provided: " + soundEvent);
        }
        markAsIgnoringSoundExceptions(soundEvent);
        SoundRemapperImpl.enable();
        var id = Identifier.of(SoundResourceGenerator.NAMESPACE, soundEvent.getNamespace() + "." + soundEvent.getPath());
        SoundRemapperImpl.register(soundEvent, id);
        SoundResourceGenerator.moveSoundEvent(soundEvent.getPath(), id.getPath());
    }

    public static ScopedOverride ignorePlaySoundExclusion() {
        SoundRemapperImpl.IGNORE_PLAY_SOUND_EXCLUSION.set(Unit.INSTANCE);
        return SoundRemapperImpl.IGNORE_PLAY_SOUND_EXCLUSION::remove;
    }
}
