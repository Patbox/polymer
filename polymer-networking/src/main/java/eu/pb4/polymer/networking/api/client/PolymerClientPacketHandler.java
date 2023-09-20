package eu.pb4.polymer.networking.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface PolymerClientPacketHandler<H extends ClientCommonNetworkHandler, T extends CustomPayload> {
    void onPacket(MinecraftClient client, H handler, T packet);
}
