package eu.pb4.polymer.ext.client.impl;

import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.networking.ServerPackets;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import static eu.pb4.polymer.api.networking.PolymerPacketUtils.buf;

public class CEProtocolServer {
    public static final String SET_RELOAD_LOGO_PACKET = "client_ext/set_reload_logo";
    public static final Identifier SET_RELOAD_LOGO_PACKET_ID = PolymerImplUtils.id("client_ext/set_reload_logo");

    public static final void initialize() {
        ServerPackets.register(SET_RELOAD_LOGO_PACKET, 0);
    }


    public static final void sendSetReloadLogo(ServerPlayNetworkHandler handler, byte[] image) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        var version = polymerHandler.polymer_getSupportedVersion(SET_RELOAD_LOGO_PACKET);

        if (version == 0) {
            var buf = buf(version);
            buf.writeByteArray(image);
        }
    }
}
