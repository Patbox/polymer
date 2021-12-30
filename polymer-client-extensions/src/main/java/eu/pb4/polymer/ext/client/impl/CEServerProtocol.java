package eu.pb4.polymer.ext.client.impl;

import eu.pb4.polymer.ext.client.api.PolymerClientExtensions;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.networking.ServerPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static eu.pb4.polymer.api.networking.PolymerPacketUtils.buf;

public class CEServerProtocol {
    public static final String SET_RELOAD_LOGO_PACKET = "client_ext/set_reload_logo";
    public static final Identifier SET_RELOAD_LOGO_PACKET_ID = PolymerImplUtils.id(SET_RELOAD_LOGO_PACKET);
    public static final String TOAST_PACKET = "client_ext/toast";
    public static final Identifier TOAST_PACKET_ID = PolymerImplUtils.id(TOAST_PACKET);


    public static final void initialize() {
        ServerPackets.register(SET_RELOAD_LOGO_PACKET, 0);
        ServerPackets.register(TOAST_PACKET, 0);
    }


    public static final boolean sendSetReload(ServerPlayNetworkHandler handler,
                                           int color, int loadingBar, int darkColor, int loadingBarDark,
                                           @Nullable PolymerClientExtensions.ReloadLogoOverride override,
                                           @Nullable byte[] texture) {

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
                if (override.requiresImage() && null != texture) {
                    buf.writeBoolean(true);
                    buf.writeByteArray(texture);
                } else {
                    buf.writeBoolean(false);
                }
            } else {
                buf.writeBoolean(false);
            }

            handler.sendPacket(new CustomPayloadS2CPacket(SET_RELOAD_LOGO_PACKET_ID, buf));
            return true;
        }

        return false;
    }

    public static final boolean sendToast(ServerPlayNetworkHandler handler, Text text, int time, @Nullable ItemStack stack, @Nullable byte[] texture) {

        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        var version = polymerHandler.polymer_getSupportedVersion(TOAST_PACKET);

        if (version == 0) {
            var buf = buf(version);
            buf.writeVarInt(time);
            buf.writeText(text);
            if (stack != null) {
                buf.writeByte(1);
                buf.writeItemStack(stack);
            } else if (texture != null) {
                buf.writeByte(2);
                buf.writeByteArray(texture);
            } else {
                buf.writeByte(0);
            }

            handler.sendPacket(new CustomPayloadS2CPacket(TOAST_PACKET_ID, buf));
            return true;
        }

        return false;
    }
}
