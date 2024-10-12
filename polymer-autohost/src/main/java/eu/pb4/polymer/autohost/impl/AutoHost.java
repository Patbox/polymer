package eu.pb4.polymer.autohost.impl;

import com.mojang.brigadier.arguments.BoolArgumentType;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.providers.AbstractProvider;
import eu.pb4.polymer.autohost.impl.providers.EmptyProvider;
import eu.pb4.polymer.autohost.impl.providers.NettyProvider;
import eu.pb4.polymer.autohost.impl.providers.StandaloneWebServerProvider;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CommonNetworkHandlerExt;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AutoHost implements ModInitializer {
    public static final Map<Identifier, Supplier<ResourcePackDataProvider>> TYPES = new HashMap<>();
    public static final List<MinecraftServer.ServerResourcePackProperties> GLOBAL_RESOURCE_PACKS = new ArrayList<>();
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
            AutoHost.message = Text.Serialization.fromJsonTree(AutoHost.config.message, server.getRegistryManager());
        } catch (Exception e) {
            AutoHost.message = null;
        }

        try {
            AutoHost.disconnectMessage = Text.Serialization.fromJsonTree(AutoHost.config.disconnectMessage, server.getRegistryManager());
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

        GLOBAL_RESOURCE_PACKS.clear();
        for (var x : config.externalResourcePacks) {
            if (x.url != null) {
                GLOBAL_RESOURCE_PACKS.add(ResourcePackDataProvider.createProperties(x.id, x.url, x.hash));
            }
        }


        CommonImpl.saveConfig("auto-host", config);

        provider.serverStarted(server);
    }

    public static void end(MinecraftServer server) {
        provider.serverStopped(server);
    }

    public static File getFile(String path) {
        var x = getPath(path);
        return x != null ? x.toFile() : null;
    }

    public static Path getPath(String path) {
        if (path.equals("main.zip")) {
            return PolymerResourcePackUtils.getMainPath();
        }

        return null;
    }

    @Override
    public void onInitialize() {
        ResourcePackDataProvider.register(Identifier.of("polymer", "automatic"), NettyProvider::new);
        ResourcePackDataProvider.register(Identifier.of("polymer", "auto"), NettyProvider::new);
        ResourcePackDataProvider.register(Identifier.of("polymer", "netty"), NettyProvider::new);
        ResourcePackDataProvider.register(Identifier.of("polymer", "same_port"), NettyProvider::new);
        ResourcePackDataProvider.register(Identifier.of("polymer", "http_server"), StandaloneWebServerProvider::new);
        ResourcePackDataProvider.register(Identifier.of("polymer", "standalone"), StandaloneWebServerProvider::new);
        ResourcePackDataProvider.register(Identifier.of("polymer", "empty"), EmptyProvider::new);

        CommonImplUtils.registerCommands((c) -> c.then(literal("generate-pack")
                        .requires(CommonImplUtils.permission("command.generate", 3))
                        .then(literal("reload").executes((ctx -> {
                            return PolymerResourcePackMod.generateResources(ctx, () -> {
                                if (!provider.isReady()) {
                                    ctx.getSource().sendMessage(Text.literal("[Polymer] AutoHost module isn't configured/loaded correctly so resource pack won't be synced!"));
                                    return;
                                }
                                for (var player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                                    for (var x : provider.getProperties(((CommonNetworkHandlerExt) player.networkHandler).polymerCommon$getConnection())) {
                                        player.networkHandler.sendPacket(new ResourcePackSendS2CPacket(x.id(), x.url(), x.hash(), AutoHost.config.require || PolymerResourcePackUtils.isRequired(), Optional.ofNullable(AutoHost.message)));
                                    }
                                }
                            });
                        })))
                )
        );

        CommonImplUtils.registerDevCommands((c) -> {
            c.then(literal("reload_resourcepack").executes(context -> {
                if (provider.isReady()) {
                    for (var x : provider.getProperties(((CommonNetworkHandlerExt) context.getSource().getPlayerOrThrow().networkHandler).polymerCommon$getConnection())) {
                        context.getSource().getPlayerOrThrow().networkHandler.sendPacket(new ResourcePackSendS2CPacket(x.id(), x.url(), x.hash(), AutoHost.config.require || PolymerResourcePackUtils.isRequired(), Optional.ofNullable(AutoHost.message)));
                    }
                }
                return 0;
            }));

            c.then(literal("force_ready").then(argument("value", BoolArgumentType.bool()).executes((ctx) -> {
                if (provider instanceof AbstractProvider abs) {
                    abs.isPackReady = BoolArgumentType.getBool(ctx, "value");
                }
                return 0;
            })));
        });
    }
}
