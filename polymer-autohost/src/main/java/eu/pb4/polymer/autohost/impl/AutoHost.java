package eu.pb4.polymer.autohost.impl;

import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.providers.NettyProvider;
import eu.pb4.polymer.autohost.impl.providers.EmptyProvider;
import eu.pb4.polymer.autohost.impl.providers.StandaloneWebServerProvider;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CommonNetworkHandlerExt;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.literal;

public class AutoHost implements ModInitializer {
    public static final Map<Identifier, Supplier<ResourcePackDataProvider>> TYPES = new HashMap<>();
    public static AutoHostConfig config = new AutoHostConfig();
    public static Text message = Text.empty();
    public static Text disconnectMessage = Text.empty();

    public static ResourcePackDataProvider provider = EmptyProvider.INSTANCE;

    public static void init(MinecraftServer server) {
        var config = CommonImpl.loadConfig("auto-host", AutoHostConfig.class);
        AutoHost.config = config;

        if (!config.enabled) {
            return;
        }

        try {
            AutoHost.message = Text.Serialization.fromJsonTree(AutoHost.config.message);
        } catch (Exception e) {
            AutoHost.message = null;
        }

        try {
            AutoHost.disconnectMessage = Text.Serialization.fromJsonTree(AutoHost.config.disconnectMessage);
        } catch (Exception e) {
            AutoHost.disconnectMessage = Text.literal("This server requires resource pack enabled to play!");
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

        provider.serverStarted(server);
    }

    public static void end(MinecraftServer server) {
        provider.serverStopped(server);
    }

    public static void generateAndCall(MinecraftServer server, Consumer<Text> messageConsumer, Runnable runnable) {
        Util.getIoWorkerExecutor().execute(() -> {
            messageConsumer.accept(Text.literal("Starting resource pack generation..."));
            boolean success = PolymerResourcePackUtils.buildMain();

            server.execute(() -> {
                if (success) {
                    messageConsumer.accept(Text.literal("Resource pack created successfully!"));
                    runnable.run();
                } else {
                    messageConsumer.accept(Text.literal("Found issues while creating resource pack! See logs above for more detail!"));
                }
            });
        });
    }

    @Override
    public void onInitialize() {
        ResourcePackDataProvider.register(new Identifier("polymer", "automatic"), NettyProvider::new);
        ResourcePackDataProvider.register(new Identifier("polymer", "auto"), NettyProvider::new);
        ResourcePackDataProvider.register(new Identifier("polymer", "netty"), NettyProvider::new);
        ResourcePackDataProvider.register(new Identifier("polymer", "same_port"), NettyProvider::new);
        ResourcePackDataProvider.register(new Identifier("polymer", "http_server"), StandaloneWebServerProvider::new);
        ResourcePackDataProvider.register(new Identifier("polymer", "standalone"), StandaloneWebServerProvider::new);
        ResourcePackDataProvider.register(new Identifier("polymer", "empty"), EmptyProvider::new);

        CommonImplUtils.registerDevCommands((c) -> {
            c.then(literal("reload_resourcepack").executes(context -> {
                if (provider.isReady()) {
                    for (var x : provider.getProperties(((CommonNetworkHandlerExt) context.getSource().getPlayerOrThrow().networkHandler).polymerCommon$getConnection()
                    )) {
                        context.getSource().getPlayerOrThrow().networkHandler.sendPacket(new ResourcePackSendS2CPacket(x.id(), x.url(), x.hash(), AutoHost.config.require || PolymerResourcePackUtils.isRequired(), AutoHost.message));
                    }
                }
                return 0;
            }));
            c.then(literal("rebuild_reload_rp").executes(context -> {
                generateAndCall(context.getSource().getServer(), context.getSource()::sendMessage, () -> {
                    for (var x : provider.getProperties(((CommonNetworkHandlerExt) context.getSource().getPlayer().networkHandler).polymerCommon$getConnection()
                    )) {
                        context.getSource().getPlayer().networkHandler.sendPacket(new ResourcePackSendS2CPacket(x.id(), x.url(), x.hash(), AutoHost.config.require || PolymerResourcePackUtils.isRequired(), AutoHost.message));
                    }
                });
                return 0;
            }));
        });
    }
}
