package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.UUID;

/**
 * This class allows for creation of custom sound effects
 * It can be used to play custom sounds for players with resourcepack while keeping fallback for vanilla clients
 */
public class PolymerSoundEvent extends SoundEvent implements PolymerSyncedObject<SoundEvent> {
    @Nullable
    protected final SoundEvent polymerSound;

    @Nullable
    protected final UUID source;

    public static PolymerSoundEvent of(Identifier identifier, @Nullable SoundEvent vanillaEvent) {
        return new PolymerSoundEvent(null, identifier, 16.0F, false, vanillaEvent);
    }

    static PolymerSoundEvent of(Identifier identifier, float distanceToTravel, @Nullable SoundEvent vanillaEvent) {
        return new PolymerSoundEvent(null, identifier, distanceToTravel, true, vanillaEvent);
    }

    public static PolymerSoundEvent of(UUID uuid, Identifier identifier, @Nullable SoundEvent vanillaEvent) {
        return new PolymerSoundEvent(uuid, identifier, 16.0F, false, vanillaEvent);
    }

    static PolymerSoundEvent of(UUID uuid, Identifier identifier, float distanceToTravel, @Nullable SoundEvent vanillaEvent) {
        return new PolymerSoundEvent(uuid, identifier, distanceToTravel, true, vanillaEvent);
    }

    public PolymerSoundEvent(@Nullable UUID uuid, Identifier id, float distanceToTravel, boolean useStaticDistance, @Nullable SoundEvent vanillaEvent) {
        super(id, distanceToTravel, useStaticDistance);
        this.source = uuid;
        this.polymerSound = vanillaEvent;
    }

    @Override
    public SoundEvent getPolymerReplacement(PacketContext context) {
        return this.source == null || this.polymerSound == null || PolymerUtils.hasResourcePack(context.getPlayer(), this.source) ? this : (this.polymerSound instanceof PolymerSoundEvent pe ? pe.getPolymerReplacement(context) : this.polymerSound);
    }
}
