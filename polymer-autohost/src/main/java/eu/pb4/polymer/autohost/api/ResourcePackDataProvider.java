package eu.pb4.polymer.autohost.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public interface ResourcePackDataProvider {
    boolean isReady();
    JsonElement saveSettings();
    void loadSettings(JsonElement settings);
    void serverStarted(MinecraftServer server);
    void serverStopped(MinecraftServer server);

    static ResourcePackDataProvider getActive() {
        return AutoHost.provider;
    }

    static <T> void register(Identifier identifier, Supplier<ResourcePackDataProvider> providerCreator) {
        AutoHost.TYPES.put(identifier, providerCreator);
    }

    Collection<MinecraftServer.ServerResourcePackProperties> getProperties(ClientConnection connection);

    String getMainFilePath(PacketContext context);
    String getFilePath(PacketContext context, Identifier identifier);

    default String getFilePath(PacketContext context, Identifier identifier, @Nullable String hash) {
        return this.getFilePath(context, identifier);
    }

    default MinecraftServer.ServerResourcePackProperties createProperties(PacketContext context, Identifier address) {
        return this.createProperties(context,null, address, null);
    }

    default MinecraftServer.ServerResourcePackProperties createProperties(PacketContext context, Identifier address, @Nullable String hash) {
        return this.createProperties(context,null, address, hash);
    }

    default MinecraftServer.ServerResourcePackProperties createProperties(PacketContext context, @Nullable UUID uuid, Identifier address, @Nullable String hash) {
        return createProperties(uuid, this.getFilePath(context, address), hash);
    }

    static MinecraftServer.ServerResourcePackProperties createProperties(String address) {
        return createProperties(null, address, null);
    }

    static MinecraftServer.ServerResourcePackProperties createProperties(String address, @Nullable String hash) {
        return createProperties(null, address, hash);
    }

    static MinecraftServer.ServerResourcePackProperties createProperties(@Nullable UUID uuid, String address, @Nullable String hash) {
        return new MinecraftServer.ServerResourcePackProperties(
                uuid != null ? uuid : UUID.nameUUIDFromBytes(address.getBytes(StandardCharsets.UTF_8)),
                address,
                hash != null ? hash : "",
                AutoHost.config.require || (AutoHost.config.modOverride && PolymerResourcePackUtils.isRequired()),
                AutoHost.message
        );
    }
}
