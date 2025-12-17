package eu.pb4.polymer.autohost.impl.providers;

import com.google.common.hash.Hashing;
import eu.pb4.polymer.autohost.api.AutoHostUtils;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
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
            PolymerResourcePackMod.generateAndCall(minecraftServer, true, minecraftServer::sendSystemMessage, () -> {});
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
    public final Collection<MinecraftServer.ServerResourcePackInfo> getProperties(Connection connection) {
        var list = new ArrayList<MinecraftServer.ServerResourcePackInfo>();
        var context = PacketContext.create(connection);

        list.add(ResourcePackDataProvider.createProperties(PolymerResourcePackUtils.getMainUuid(), this.getMainFilePath(context), this.hash));
        AutoHostUtils.SEND_RESOURCE_PACK_COLLECTOR.invoke(x -> x.collectSendResourcePacks(this, context, list::add));
        return list;
    }

    @Override
    public String getFilePath(PacketContext context, Identifier identifier, @Nullable String hash) {
        if (!AutoHost.config.includeHashInName) {
            return getFilePath(context, identifier);
        }

        return getAddress(context.getClientConnection(), AutoHostUtils.getPathFromId(identifier) + "+" + hash + ".zip");
    }

    @Override
    public String getFilePath(PacketContext context, Identifier identifier) {
        return getAddress(context.getClientConnection(), AutoHostUtils.getPathFromId(identifier) + "+pack.zip");
    }

    @Override
    public String getMainFilePath(PacketContext context) {
        return getFilePath(context, AutoHostUtils.DEFAULT_PACK_ID, hash);
    }

    protected abstract String getAddress(Connection connection, String path);

    @Override
    public boolean isReady() {
        return this.isPackReady;
    }
}