package eu.pb4.polymer.autohost.impl;

import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.providers.EmptyProvider;
import eu.pb4.polymer.autohost.impl.providers.WebServerProvider;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.networking.api.EarlyPlayNetworkHandler;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.literal;

public class AutoHost implements ModInitializer {
    public static final Map<Identifier, Supplier<ResourcePackDataProvider>> TYPES = new HashMap<>();
    public static AutoHostConfig config;
    public static Text message;
    public static Text disconnectMessage;

    public static ResourcePackDataProvider provider = EmptyProvider.INSTANCE;

    public static void init(MinecraftServer server) {
        if (!config.enabled) {
            return;
        }

        var type = TYPES.get(Identifier.tryParse(config.type));


        if (type == null) {
            return;
        }

        provider = type.get();


        if (config.providerSettings != null) {
            provider.loadSettings(config.providerSettings);
        }

        config.providerSettings = provider.saveSettings();

        CommonImpl.saveConfig("auto-host", config);


        EarlyPlayNetworkHandler.register(ResourcePackNetworkHandler::new);
        provider.serverStarted(server);
    }

    public static void generateAndCall(MinecraftServer server, Runnable runnable) {
        Util.getIoWorkerExecutor().execute(() -> {
            server.sendMessage(Text.literal("Starting resource pack generation..."));
            boolean success = PolymerResourcePackUtils.build();

            server.execute(() -> {
                if (success) {
                    server.sendMessage(Text.literal("Resource pack created successfully!"));
                    runnable.run();
                } else {
                    server.sendMessage(Text.literal("Found issues while creating resource pack! See logs above for more detail!"));
                }
            });
        });
    }

    @Override
    public void onInitialize() {
        ResourcePackDataProvider.register(new Identifier("polymer", "http_server"), WebServerProvider::new);
        ResourcePackDataProvider.register(new Identifier("polymer", "empty"), EmptyProvider::new);

        var config = CommonImpl.loadConfig("auto-host", AutoHostConfig.class);
        AutoHost.config = config;

        if (!config.enabled) {
            return;
        }

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

        CommonImplUtils.registerDevCommands((c) -> {
            c.then(literal("reload_resourcepack").executes(context -> {
                if (provider.isReady()) {
                    context.getSource().getPlayerOrThrow().networkHandler.sendPacket(new ResourcePackSendS2CPacket(provider.getAddress(), provider.getHash(), AutoHost.config.require || PolymerResourcePackUtils.isRequired(), AutoHost.message));
                }
                return 0;
            }));
        });
    }
}
