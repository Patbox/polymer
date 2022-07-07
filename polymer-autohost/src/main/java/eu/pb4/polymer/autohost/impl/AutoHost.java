package eu.pb4.polymer.autohost.impl;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.x.EarlyPlayNetworkHandler;
import eu.pb4.polymer.impl.PolymerImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class AutoHost {
    public static WebServer.Config config;
    public static Text message;
    public static Text disconnectMessage;


    public static void init(MinecraftServer server) {
        var config = PolymerImpl.loadConfig("auto-host", WebServer.Config.class);
        AutoHost.config = config;

        try {
            AutoHost.message = Text.Serializer.fromJson(AutoHost.config.message);
        } catch (Exception e) {
            AutoHost.message = null;
        }

        try {
            AutoHost.disconnectMessage = Text.Serializer.fromJson(AutoHost.config.disconnectMessage);
        } catch (Exception e) {
            AutoHost.disconnectMessage = Text.literal("This server requires resource pack enabled to play!");
        }

        if (config.enableHttpServer) {
            EarlyPlayNetworkHandler.register(ResourcePackNetworkHandler::new);

            Util.getIoWorkerExecutor().execute(() -> {
                server.sendMessage(Text.literal("Starting resource pack generation..."));
                boolean success = PolymerRPUtils.build(PolymerRPUtils.DEFAULT_PATH);

                server.execute(() -> {
                    if (success) {
                        server.sendMessage(Text.literal("Resource pack created successfully!"));

                        WebServer.start(server, config);
                    } else {
                        server.sendMessage(Text.literal("Found issues while creating resource pack! See logs above for more detail!"));
                    }
                });
            });
        }
    }
}
