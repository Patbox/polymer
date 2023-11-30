package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlaySoundFromEntityS2CPacket.class)
public interface PlaySoundFromEntityS2CPacketAccessor {
    @Mutable
    @Accessor
    void setSound(RegistryEntry<SoundEvent> sound);

    @Mutable
    @Accessor
    void setCategory(SoundCategory category);

    @Mutable
    @Accessor
    void setEntityId(int entityId);

    @Mutable
    @Accessor
    void setVolume(float volume);

    @Mutable
    @Accessor
    void setPitch(float pitch);

    @Mutable
    @Accessor
    void setSeed(long seed);
}
