package eu.pb4.polymer.autohost.impl.providers;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;
import eu.pb4.polymer.autohost.api.AutoHostUtils;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractProvider implements ResourcePackDataProvider {
    public long size = 0;
    public String hash = "";
    public long lastUpdate = 0;
    public boolean enabled;
    public boolean isPackReady = false;
    private Consumer<ResourcePackBuilder> eventA;
    private Runnable eventB;

    public void serverStarted(MinecraftServer minecraftServer) {
        this.enabled = true;

        this.isPackReady = false;

        this.eventA = PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.registerRet((x) -> {
            isPackReady = false;
        });

        this.eventB = PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.registerRet(() -> {
            updateHash();
            isPackReady = true;
        });

        try {
            PolymerResourcePackMod.generateAndCall(minecraftServer, true, minecraftServer::sendMessage, () -> {});
        } catch (Throwable e) {
            CommonImpl.LOGGER.warn("Failed to generate the resource pack!", e);
        }
    }

    @Override
    public void serverStopped(MinecraftServer server) {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.unregister(this.eventA);
        PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.unregister(this.eventB);
    }

    protected boolean updateHash() {
        try {
            if (Files.exists(PolymerResourcePackUtils.getMainPath())) {
                hash = com.google.common.io.Files.asByteSource(PolymerResourcePackUtils.getMainPath().toFile()).hash(Hashing.sha1()).toString();
                size = Files.size(PolymerResourcePackUtils.getMainPath());
                lastUpdate = Files.getLastModifiedTime(PolymerResourcePackUtils.getMainPath()).toMillis();
                return true;
            }
        } catch (Exception e) {

        }
        hash = "";
        size = 0;
        return false;
    }

    @Override
    public final Collection<MinecraftServer.ServerResourcePackProperties> getProperties(ClientConnection connection) {
        var list = new ArrayList<MinecraftServer.ServerResourcePackProperties>();
        var context = PacketContext.create(connection);

        list.add(ResourcePackDataProvider.createProperties(PolymerResourcePackUtils.getMainUuid(), this.getMainFilePath(context), this.hash));
        AutoHostUtils.SEND_RESOURCE_PACK_COLLECTOR.invoke(x -> x.collectSendResourcePacks(this, context, list::add));
        return list;
    }

    @Override
    public String getFilePath(PacketContext context, Identifier identifier) {
        return getAddress(context.getClientConnection(), identifier.getNamespace() + "/" + identifier.getPath());
    }

    @Override
    public String getMainFilePath(PacketContext context) {
        return getAddress(context.getClientConnection(), "main_" + hash + ".zip");
    }

    protected abstract String getAddress(ClientConnection connection, String path);

    @Override
    public boolean isReady() {
        return this.isPackReady;
    }
}