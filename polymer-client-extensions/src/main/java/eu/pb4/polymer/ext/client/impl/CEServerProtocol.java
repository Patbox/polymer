package eu.pb4.polymer.ext.client.impl;

import eu.pb4.polymer.ext.client.api.PolymerClientExtensions;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.networking.ServerPackets;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static eu.pb4.polymer.api.networking.PolymerPacketUtils.buf;

public class CEServerProtocol {
    public static final String SET_RELOAD_LOGO_PACKET = "client_ext/set_reload_logo";
    public static final Identifier SET_RELOAD_LOGO_PACKET_ID = PolymerImplUtils.id("client_ext/set_reload_logo");

    public static final void initialize() {
        ServerPackets.register(SET_RELOAD_LOGO_PACKET, 0);
    }


    public static final void sendSetReloadLogo(ServerPlayNetworkHandler handler,
                                               int color, int loadingBar, int darkColor, int loadingBarDark,
                                               @Nullable PolymerClientExtensions.ReloadLogoOverride override,
                                               @Nullable String base64Texture) {

        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        var version = polymerHandler.polymer_getSupportedVersion(SET_RELOAD_LOGO_PACKET);

        if (version == 0) {
            var buf = buf(version);
            if (override != null) {
                buf.writeBoolean(true);
                buf.writeInt(color);
                buf.writeInt(loadingBar);
                buf.writeInt(darkColor);
                buf.writeInt(loadingBarDark);
                buf.writeByte(override.ordinal());
                if (override.requiresImage() && base64Texture != null) {
                    buf.writeBoolean(true);
                    buf.writeString(base64Texture);
                } else {
                    buf.writeBoolean(false);
                }
            } else {
                buf.writeBoolean(false);
            }

            handler.sendPacket(new CustomPayloadS2CPacket(SET_RELOAD_LOGO_PACKET_ID, buf));
        }

    }
}
