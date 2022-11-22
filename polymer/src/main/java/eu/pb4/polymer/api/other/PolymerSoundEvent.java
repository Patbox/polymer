package eu.pb4.polymer.api.other;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.impl.PolymerImplUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * This class allows for creation of custom sound effects
 * It can be used to play custom sounds for players with resourcepack while keeping fallback for vanilla clients
 */
public class PolymerSoundEvent extends SoundEvent implements PolymerSyncedObject<SoundEvent> {
    public static final SoundEvent EMPTY_SOUND = new SoundEvent(PolymerImplUtils.id("empty_sound"));
    private final SoundEvent polymerSound;

    public PolymerSoundEvent(Identifier id, @Nullable SoundEvent vanillaEvent) {
        super(id);
        while (vanillaEvent instanceof PolymerSoundEvent soundEvent) {
            vanillaEvent = soundEvent.getVanillaPolymerSound();
        }

        this.polymerSound = vanillaEvent != null ? vanillaEvent : EMPTY_SOUND;
    }

    public PolymerSoundEvent(Identifier id, float distanceToTravel, @Nullable SoundEvent vanillaEvent) {
        super(id, distanceToTravel);
        while (vanillaEvent instanceof PolymerSoundEvent soundEvent) {
            vanillaEvent = soundEvent.getVanillaPolymerSound();
        }

        this.polymerSound = vanillaEvent != null ? vanillaEvent : EMPTY_SOUND;
    }

    /**
     * SoundEvent played for players without resource pack
     */
    public SoundEvent getVanillaPolymerSound() {
        return this.polymerSound;
    }

    @Override
    public SoundEvent getPolymerReplacement(ServerPlayerEntity player) {
        return PolymerRPUtils.hasPack(player) ? this : this.getVanillaPolymerSound();
    }
}
