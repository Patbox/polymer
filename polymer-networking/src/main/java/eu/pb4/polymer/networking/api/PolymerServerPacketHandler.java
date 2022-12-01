package eu.pb4.polymer.networking.api;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@FunctionalInterface
public interface PolymerServerPacketHandler {
    void onPacket(ServerPlayNetworkHandler handler, int version, PacketByteBuf buf);
}
