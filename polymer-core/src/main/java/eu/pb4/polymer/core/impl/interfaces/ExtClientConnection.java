package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

import java.util.function.Consumer;

public interface ExtClientConnection {
    void polymer$ignorePacketsUntilChange(Consumer<CustomPayloadC2SPacket> consumer);
}
