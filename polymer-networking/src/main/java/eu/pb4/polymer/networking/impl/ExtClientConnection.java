package eu.pb4.polymer.networking.impl;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public interface ExtClientConnection {
    void polymerNet$ignorePacketsUntilChange(Consumer<CustomPayloadC2SPacket> consumer);
}
