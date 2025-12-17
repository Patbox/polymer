package eu.pb4.polymer.networking.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface PolymerClientPacketHandler<H extends ClientCommonPacketListenerImpl, T extends CustomPacketPayload> {
    void onPacket(Minecraft client, H handler, T packet);
}
