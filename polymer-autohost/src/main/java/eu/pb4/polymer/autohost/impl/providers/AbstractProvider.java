package eu.pb4.polymer.autohost.impl.providers;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

public abstract class AbstractProvider implements ResourcePackDataProvider {
    public long size = 0;
    public String hash = "";
    public long lastUpdate = 0;
    public boolean enabled;
    public boolean isPackReady = false;

    public void serverStarted(MinecraftServer minecraftServer) {
        this.enabled = true;

        this.isPackReady = true;
        updateHash();

        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((x) -> {
            isPackReady = false;
        });

        PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.register(() -> {
            isPackReady = true;
            updateHash();
        });

        AutoHost.generateAndCall(minecraftServer, minecraftServer::sendMessage, () -> {
        });
    }

    protected boolean updateHash() {
        try {
            hash = com.google.common.io.Files.asByteSource(PolymerResourcePackUtils.getMainPath().toFile()).hash(Hashing.sha1()).toString();
            size = Files.size(PolymerResourcePackUtils.getMainPath());
            lastUpdate = Files.getLastModifiedTime(PolymerResourcePackUtils.getMainPath()).toMillis();
            return true;
        } catch (Exception e) {
            hash = "";
            size = 0;
            return false;
        }

    }

    @Override
    public Collection<MinecraftServer.ServerResourcePackProperties> getProperties(ClientConnection connection) {
        return List.of(ResourcePackDataProvider.createProperties(PolymerResourcePackUtils.getMainUuid(), this.getAddress(connection), this.hash));
    }

    protected abstract String getAddress(ClientConnection connection);

    @Override
    public boolean isReady() {
        return this.isPackReady;
    }
}