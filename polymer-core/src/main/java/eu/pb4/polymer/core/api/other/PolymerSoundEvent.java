package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
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
    private final SoundEvent self;

    public static PolymerSoundEvent of(Identifier identifier, SoundEvent self, @Nullable SoundEvent vanillaEvent) {
        return new PolymerSoundEvent(null, self, vanillaEvent);
    }

    public PolymerSoundEvent(@Nullable UUID uuid, SoundEvent self, @Nullable SoundEvent vanillaEvent) {
        this.source = uuid;
        this.self = self;
        this.polymerSound = vanillaEvent;
    }

    @Override
    public SoundEvent getPolymerReplacement(PacketContext context) {
        return this.source == null || this.polymerSound == null || PolymerUtils.hasResourcePack(context.getPlayer(), this.source) ? this.self : this.polymerSound;
    }
}
