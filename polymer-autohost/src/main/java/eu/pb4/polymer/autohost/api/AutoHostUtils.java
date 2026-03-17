package eu.pb4.polymer.autohost.api;

import eu.pb4.polymer.autohost.impl.AutoHost;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.function.Consumer;

public class AutoHostUtils {
    public static Identifier DEFAULT_PACK_ID = Identifier.fromNamespaceAndPath("polymer", "resources");
    public static Event<SendResourcePackCollector> SEND_RESOURCE_PACK_COLLECTOR = EventFactory.createArrayBacked(SendResourcePackCollector.class, arr ->
            (provider, context, consumer) -> {
                for (var a : arr) {
                    a.collectSendResourcePacks(provider, context, consumer);
                }
            });

    public static Event<ResourcePacksReadyCheck> RESOURCE_PACKS_READY = EventFactory.createArrayBacked(ResourcePacksReadyCheck.class, arr ->
            (provider, context) -> {
                var res = true;
                for (var a : arr) {
                    res &= a.areResourcePacksReady(provider, context);
                }
                return res;
            });

    private AutoHostUtils() {
    }

    public static String registerHostedFile(Identifier identifier, Path path) {
        var string = getPathFromId(identifier);
        AutoHost.FILES.put(string, path);
        return string;
    }

    public static String getPathFromId(Identifier id) {
        return id.getNamespace() + "/" + id.getPath();
    }

    public static void requestPackGenerationWhenDisabled() {
        AutoHost.generateWhenDisabled = true;
    }

    public interface SendResourcePackCollector {
        void collectSendResourcePacks(ResourcePackDataProvider provider, PacketContext context,
                                      Consumer<MinecraftServer.ServerResourcePackInfo> consumer);
    }

    public interface ResourcePacksReadyCheck {
        boolean areResourcePacksReady(ResourcePackDataProvider provider, PacketContext context);
    }
}
