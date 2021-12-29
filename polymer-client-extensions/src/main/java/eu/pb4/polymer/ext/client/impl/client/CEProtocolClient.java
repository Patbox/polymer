package eu.pb4.polymer.ext.client.impl.client;

import eu.pb4.polymer.ext.client.impl.CEProtocolServer;
import eu.pb4.polymer.impl.client.networking.PolymerClientProtocolHandler;
import eu.pb4.polymer.impl.networking.ServerPackets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.data.client.model.Texture;
import net.minecraft.network.PacketByteBuf;

import javax.imageio.ImageIO;

public class CEProtocolClient {
    public static final void initialize() {
        PolymerClientProtocolHandler.CUSTOM_PACKETS.put(CEProtocolServer.SET_RELOAD_LOGO_PACKET, CEProtocolClient::handleSetReloadLogo);
    }

    private static void handleSetReloadLogo(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var data = buf.readByteArray();
            var client = MinecraftClient.getInstance();

            client.getTextureManager().registerTexture(CERegistry.RELOAD_LOGO_IDENTIFIER, AbstractTexture);
            RELOAD_LOGO = null;
            client.getTextureManager().destroyTexture(RELOAD_LOGO_IDENTIFIER);
        }
    }
}
