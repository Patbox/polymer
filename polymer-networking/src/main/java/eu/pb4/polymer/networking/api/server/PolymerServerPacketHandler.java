package eu.pb4.polymer.networking.api.server;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@FunctionalInterface
public interface PolymerServerPacketHandler<H extends ServerCommonNetworkHandler, T extends CustomPayload> {
    void onPacket(MinecraftServer server, H handler, T packet);
}
