package eu.pb4.polymer.api.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

@FunctionalInterface
public interface PolymerClientPacketHandler {
    void onPacket(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf);
}
