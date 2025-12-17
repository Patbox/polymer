package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSoundEntityPacket.class)
public interface ClientboundSoundEntityPacketAccessor {
    @Mutable
    @Accessor
    void setSound(Holder<SoundEvent> sound);

    @Mutable
    @Accessor
    void setSource(SoundSource category);

    @Mutable
    @Accessor
    void setId(int entityId);

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
