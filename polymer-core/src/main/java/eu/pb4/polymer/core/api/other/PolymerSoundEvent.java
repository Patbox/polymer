package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

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
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.SOUND_EVENT, event, (object, context) -> object);
        return event;
    }

    public static SoundEvent registerOverlay(SoundEvent event, @Nullable SoundEvent fallback) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.SOUND_EVENT, event, of(fallback));
        return event;
    }

    public static SoundEvent registerOverlay(SoundEvent event, Holder<SoundEvent> fallback) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.SOUND_EVENT, event, of(fallback.value()));
        return event;
    }

    public static SoundEvent registerOverlay(SoundEvent event, @Nullable SoundEvent fallback, @Nullable UUID resourcePackUuid) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.SOUND_EVENT, event, new PolymerSoundEvent(resourcePackUuid, fallback));
        return event;
    }

    public static SoundEvent registerOverlay(SoundEvent event, Holder<SoundEvent> fallback, @Nullable UUID resourcePackUuid) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.SOUND_EVENT, event, new PolymerSoundEvent(resourcePackUuid, fallback.value()));
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
        return this.source == null || this.polymerSound == null || PolymerCommonUtils.hasResourcePack(context, this.source) ? event : this.polymerSound;
    }
}
