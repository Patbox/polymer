package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

@ApiStatus.Internal
public class ServerMetadataKeys {
    public static final Identifier MINECRAFT_VERSION = id("minecraft_version");
    public static final Identifier MINECRAFT_PROTOCOL = id("minecraft_protocol");
    public static final Identifier LIMITED_F3 = id("settings/limited_f3");

    public static void setup() {
        PolymerServerNetworking.setServerMetadata(MINECRAFT_VERSION, NbtString.of(SharedConstants.getGameVersion().getName()));
        PolymerServerNetworking.setServerMetadata(MINECRAFT_PROTOCOL, NbtInt.of(SharedConstants.getProtocolVersion()));
        PolymerServerNetworking.setServerMetadata(LIMITED_F3, NbtByte.of(false));
    }
}
