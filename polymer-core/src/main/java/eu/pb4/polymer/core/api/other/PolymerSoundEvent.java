package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.UUID;

/**
 * This class allows for creation of custom sound effects
 * It can be used to play custom sounds for players with resourcepack while keeping fallback for vanilla clients
 */
public class PolymerSoundEvent implements PolymerSyncedObject<SoundEvent> {
    @Nullable
    protected final SoundEvent polymerSound;
    @Nullable
    protected final UUID source;

    public static SoundEvent registerOverlay(SoundEvent event) {
        PolymerSyncedObject.setSyncedObject(Registries.SOUND_EVENT, event, (object, context) -> object);
        return event;
    }

    public static SoundEvent registerOverlay(SoundEvent event, @Nullable SoundEvent fallback) {
        PolymerSyncedObject.setSyncedObject(Registries.SOUND_EVENT, event, of(fallback));
        return event;
    }

    public static SoundEvent registerOverlay(SoundEvent event, RegistryEntry<SoundEvent> fallback) {
        PolymerSyncedObject.setSyncedObject(Registries.SOUND_EVENT, event, of(fallback.value()));
        return event;
    }

    public static SoundEvent registerOverlay(SoundEvent event, @Nullable SoundEvent fallback, @Nullable UUID resourcePackUuid) {
        PolymerSyncedObject.setSyncedObject(Registries.SOUND_EVENT, event, new PolymerSoundEvent(resourcePackUuid, fallback));
        return event;
    }

    public static SoundEvent registerOverlay(SoundEvent event, RegistryEntry<SoundEvent> fallback, @Nullable UUID resourcePackUuid) {
        PolymerSyncedObject.setSyncedObject(Registries.SOUND_EVENT, event, new PolymerSoundEvent(resourcePackUuid, fallback.value()));
        return event;
    }

    public static PolymerSoundEvent of(@Nullable SoundEvent vanillaEvent) {
        return new PolymerSoundEvent(null, vanillaEvent);
    }

    public PolymerSoundEvent(@Nullable UUID uuid, @Nullable SoundEvent vanillaEvent) {
        this.source = uuid;
        this.polymerSound = vanillaEvent;
    }

    @Override
    public SoundEvent getPolymerReplacement(SoundEvent event, PacketContext context) {
        return this.source == null || this.polymerSound == null || PolymerUtils.hasResourcePack(context.getPlayer(), this.source) ? event : this.polymerSound;
    }
}
