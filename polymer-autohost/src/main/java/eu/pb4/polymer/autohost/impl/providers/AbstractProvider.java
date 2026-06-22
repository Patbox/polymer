package eu.pb4.polymer.autohost.impl.providers;

import com.google.common.hash.Hashing;
import eu.pb4.polymer.autohost.api.AutoHostUtils;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractProvider implements ResourcePackDataProvider {
    public long size = 0;
    public String hash = "";
    public long lastUpdate = 0;

    public boolean enabled;

    public void serverStarted(MinecraftServer minecraftServer) {
        this.enabled = true;
        PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.register(_ -> updateHash());
    }

    @Override
    public void serverStopped(MinecraftServer server) {

    }

    protected boolean updateHash() {
        try {
            if (Files.exists(PolymerResourcePackUtils.getMainPath())) {
                hash = com.google.common.io.Files.asByteSource(PolymerResourcePackUtils.getMainPath().toFile()).hash(Hashing.sha1()).toString();
                size = Files.size(PolymerResourcePackUtils.getMainPath());
                lastUpdate = Files.getLastModifiedTime(PolymerResourcePackUtils.getMainPath()).toMillis();
                return true;
            }
        } catch (Exception _) {

        }
        hash = "";
        size = 0;
        return false;
    }

    @Override
    public final Collection<MinecraftServer.ServerResourcePackInfo> getProperties(PacketContext context) {
        var list = new ArrayList<MinecraftServer.ServerResourcePackInfo>();

        AutoHostUtils.SEND_RESOURCE_PACK_COLLECTOR.invoker().collectSendResourcePacks(this, context, list::add);
        return list;
    }

    @Override
    public String getFilePath(PacketContext context, Identifier identifier, @Nullable String hash) {
        if (!AutoHost.config.includeHashInName) {
            return getFilePath(context, identifier);
        }

        return getAddress(context.orElseThrow(PacketContext.CONNECTION), AutoHostUtils.getPathFromId(identifier) + "+" + hash + ".zip");
    }

    @Override
    public String getFilePath(PacketContext context, Identifier identifier) {
        return getAddress(context.orElseThrow(PacketContext.CONNECTION), AutoHostUtils.getPathFromId(identifier) + "+pack.zip");
    }

    @Override
    public String getMainFilePath(PacketContext context) {
        return getFilePath(context, AutoHostUtils.DEFAULT_PACK_ID, hash);
    }

    protected abstract String getAddress(Connection connection, String path);

    @Override
    public boolean isReady(PacketContext context) {
        return AutoHostUtils.RESOURCE_PACKS_READY.invoker().areResourcePacksReady(this, context);
    }
}