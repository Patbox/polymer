package eu.pb4.polymer.core.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public interface ClientboundPlayerInfoUpdatePacketAccessor {
    @Mutable
    @Accessor
    void setEntries(List<ClientboundPlayerInfoUpdatePacket.Entry> entries);
}
