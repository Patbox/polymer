package eu.pb4.polymer.networking.api.server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@FunctionalInterface
public interface PolymerServerPacketHandler<H extends ServerCommonPacketListenerImpl, T extends CustomPacketPayload> {
    void onPacket(MinecraftServer server, H handler, T packet);
}
