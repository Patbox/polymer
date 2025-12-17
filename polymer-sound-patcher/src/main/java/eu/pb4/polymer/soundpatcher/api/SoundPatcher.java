package eu.pb4.polymer.soundpatcher.api;

import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundsAsset;
import eu.pb4.polymer.soundpatcher.impl.SoundPatchImpl;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import eu.pb4.polymer.soundpatcher.impl.SoundResourceGenerator;
import eu.pb4.polymer.soundpatcher.impl.VanillaSoundJson;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public final class SoundPatcher {
    private SoundPatcher(){}
    public static void markAsIgnoringSoundExclusions(SoundType soundGroup) {
        markAsIgnoringSoundExclusions(soundGroup.getStepSound());
        markAsIgnoringSoundExclusions(soundGroup.getBreakSound());
        markAsIgnoringSoundExclusions(soundGroup.getFallSound());
        markAsIgnoringSoundExclusions(soundGroup.getHitSound());
        markAsIgnoringSoundExclusions(soundGroup.getPlaceSound());
    }
    
    public static void markAsIgnoringSoundExclusions(SoundEvent soundEvent) {
        markAsIgnoringSoundExclusions(soundEvent.location());
    }

    public static void markAsIgnoringSoundExclusions(Identifier id) {
        SoundRemapperImpl.enable();
        SoundRemapperImpl.SOUND_EXCEPTION_IGNORER.add(id);
    }

    public static void convertAllVanillaBlockSoundsIntoServerSounds() {
        SoundPatchImpl.VANILLA_BLOCK_SOUNDS = true;
    }

    public static void convertIntoServerSound(SoundEvent soundEvent) {
        convertIntoServerSound(soundEvent.location());
    }

    public static void convertIntoServerSound(SoundType soundGroup) {
        convertIntoServerSound(soundGroup.getStepSound());
        convertIntoServerSound(soundGroup.getBreakSound());
        convertIntoServerSound(soundGroup.getFallSound());
        convertIntoServerSound(soundGroup.getHitSound());
        convertIntoServerSound(soundGroup.getPlaceSound());
    }

    public static SoundsAsset getVanillaSoundAsset() {
        return VanillaSoundJson.getSoundAsset();
    }

    public static void convertIntoServerSound(Identifier soundEvent) {
        if (!soundEvent.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            throw new IllegalArgumentException("Only vanilla / minecraft sound events are supported, provided: " + soundEvent);
        }
        markAsIgnoringSoundExclusions(soundEvent);
        SoundRemapperImpl.enable();
        var id = Identifier.fromNamespaceAndPath(SoundResourceGenerator.NAMESPACE, soundEvent.getNamespace() + "." + soundEvent.getPath());
        if (SoundRemapperImpl.register(soundEvent, id)) {
            SoundResourceGenerator.moveSoundEvent(soundEvent.getPath(), id.getPath());
        }
    }
}
