package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

@ApiStatus.Internal
public class ServerMetadataKeys {
    public static final Identifier MINECRAFT_VERSION = id("minecraft_version");
    public static final Identifier MINECRAFT_PROTOCOL = id("minecraft_protocol");
    public static final Identifier LIMITED_F3 = id("settings/limited_f3");

    public static void setup() {
        PolymerServerNetworking.setServerMetadata(MINECRAFT_VERSION, StringTag.valueOf(SharedConstants.getCurrentVersion().name()));
        PolymerServerNetworking.setServerMetadata(MINECRAFT_PROTOCOL, IntTag.valueOf(SharedConstants.getProtocolVersion()));
        PolymerServerNetworking.setServerMetadata(LIMITED_F3, ByteTag.valueOf(false));
    }
}
