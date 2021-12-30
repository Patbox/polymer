package eu.pb4.polymer.ext.client.impl.client;

import eu.pb4.polymer.ext.client.api.PolymerClientExtensions;
import eu.pb4.polymer.ext.client.impl.CEServerProtocol;
import eu.pb4.polymer.impl.client.networking.PolymerClientProtocolHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import java.io.ByteArrayInputStream;

@Environment(EnvType.CLIENT)
public class CEClientProtocol {
    public static final void initialize() {
        PolymerClientProtocolHandler.CUSTOM_PACKETS.put(CEServerProtocol.SET_RELOAD_LOGO_PACKET, CEClientProtocol::handleSetReloadLogo);
        PolymerClientProtocolHandler.CUSTOM_PACKETS.put(CEServerProtocol.TOAST_PACKET, CEClientProtocol::handleToast);
    }

    private static void handleSetReloadLogo(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var client = MinecraftClient.getInstance();

            try {
                if (buf.readBoolean()) {
                    var color = buf.readInt() & 0xFFFFFF;
                    var loadingBar = buf.readInt() & 0xFFFFFF;
                    var darkColor = buf.readInt() & 0xFFFFFF;
                    var loadingBarDark = buf.readInt() & 0xFFFFFF;
                    var type = buf.readEnumConstant(PolymerClientExtensions.ReloadLogoOverride.class);
                    byte[] data;
                    if (buf.readBoolean()) {
                        data = buf.readByteArray();
                    } else {
                        data = null;
                    }

                    client.execute(() -> {
                        try {
                            if (data != null) {
                                var texture = ClientImplUtils.generateTexture(data);
                                CERegistry.customReloadTextureHeight = texture.getImage().getHeight();
                                CERegistry.customReloadTextureWidth = texture.getImage().getWidth();
                                client.getTextureManager().registerTexture(CERegistry.RELOAD_LOGO_IDENTIFIER, texture);
                            }
                            CERegistry.customReloadMode = type;
                            CERegistry.customReloadColor = color;
                            CERegistry.customReloadColorBar = loadingBar;
                            CERegistry.customReloadColorDark = darkColor;
                            CERegistry.customReloadColorBarDark = loadingBarDark;

                            CERegistry.customReloadLogo = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    client.execute(() -> {
                        if (CERegistry.customReloadLogo) {
                            client.getTextureManager().destroyTexture(CERegistry.RELOAD_LOGO_IDENTIFIER);
                            CERegistry.customReloadLogo = false;
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleToast(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var client = MinecraftClient.getInstance();
            var time = buf.readVarInt();
            var text = buf.readText();

            var type = buf.readByte();
            var stack = type == 1 ? buf.readItemStack() : null;
            var data = type == 2 ? buf.readByteArray() : null;

            client.execute(() -> {
                client.getToastManager().add(new CEToastImpl(text, time, stack, data));
            });
        }
    }
}
