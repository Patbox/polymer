package eu.pb4.polymer.autohost.api;

import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.nio.file.Path;
import java.util.function.Consumer;

public class AutoHostUtils {
    private AutoHostUtils() {}

    public static SimpleEvent<SendResourcePackCollector> SEND_RESOURCE_PACK_COLLECTOR = new SimpleEvent<>();

    public static String registerHostedFile(Identifier identifier, Path path) {
        var string = identifier.getNamespace() + "/" + identifier.getPath();
        AutoHost.FILES.put(string, path);
        return string;
    }

    public interface SendResourcePackCollector {
        void collectSendResourcePacks(ResourcePackDataProvider provider, PacketContext context,
                                      Consumer<MinecraftServer.ServerResourcePackProperties> consumer);
    }
}
