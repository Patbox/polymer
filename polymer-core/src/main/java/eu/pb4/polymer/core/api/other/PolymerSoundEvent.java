package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * This class allows for creation of custom sound effects
 * It can be used to play custom sounds for players with resourcepack while keeping fallback for vanilla clients
 */
public class PolymerSoundEvent extends SoundEvent implements PolymerSyncedObject<SoundEvent> {
    public static final SoundEvent EMPTY_SOUND = new SoundEvent(PolymerImplUtils.id("empty_sound"), 0, false);
    private final SoundEvent polymerSound;

    public static PolymerSoundEvent of(Identifier identifier, @Nullable SoundEvent vanillaEvent) {
        return new PolymerSoundEvent(identifier, 16.0F, false, vanillaEvent);
    }

    static PolymerSoundEvent of(Identifier identifier, float distanceToTravel, @Nullable SoundEvent vanillaEvent) {
        return new PolymerSoundEvent(identifier, distanceToTravel, true, vanillaEvent);
    }


    public PolymerSoundEvent(Identifier id, float distanceToTravel, boolean useStaticDistance, @Nullable SoundEvent vanillaEvent) {
        super(id, distanceToTravel, useStaticDistance);

        this.polymerSound = vanillaEvent != null ? vanillaEvent : EMPTY_SOUND;
    }

    @Override
    public SoundEvent getPolymerReplacement(ServerPlayerEntity player) {
        return PolymerUtils.hasResourcePack(player) ? this : (this.polymerSound instanceof PolymerSoundEvent pe ? pe.getPolymerReplacement(player) : this.polymerSound);
    }
}
