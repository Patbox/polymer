package eu.pb4.polymer.autohost.impl.providers;

import eu.pb4.polymer.autohost.api.AutoHostUtils;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.AutoHost;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractProvider implements ResourcePackDataProvider {
    public boolean enabled;

    public void serverStarted(MinecraftServer minecraftServer) {
        this.enabled = true;
    }

    @Override
    public void serverStopped(MinecraftServer server) {

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

    protected abstract String getAddress(Connection connection, String path);

    @Override
    public boolean isReady(PacketContext context) {
        return AutoHostUtils.RESOURCE_PACKS_READY.invoker().areResourcePacksReady(this, context);
    }
}