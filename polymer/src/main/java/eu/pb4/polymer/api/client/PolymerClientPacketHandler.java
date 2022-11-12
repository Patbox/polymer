package eu.pb4.polymer.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface PolymerClientPacketHandler {
    void onPacket(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf);
}
