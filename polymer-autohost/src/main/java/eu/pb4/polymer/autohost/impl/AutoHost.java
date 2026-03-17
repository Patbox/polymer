package eu.pb4.polymer.autohost.impl;

import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.autohost.api.AutoHostUtils;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.providers.*;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CommonPacketListenerImplExt;
import eu.pb4.polymer.resourcepack.api.OutputGenerator;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.literal;

public class AutoHost implements ModInitializer {
    public static final Map<Identifier, Supplier<ResourcePackDataProvider>> TYPES = new HashMap<>();
    public static final List<MinecraftServer.ServerResourcePackInfo> GLOBAL_RESOURCE_PACKS = new ArrayList<>();
    public static final Map<String, Path> FILES = new HashMap<>();
    public static AutoHostConfig config = new AutoHostConfig();
    public static Component message = Component.empty();
    public static Component disconnectMessage = Component.empty();
    public static Component dialogTitle = Component.empty();
    public static Component dialogDefaultBody = Component.empty();
    public static Component dialogHeader = Component.empty();
    public static ResourcePackDataProvider provider = EmptyProvider.INSTANCE;

    public static final String DEFAULT_PATH = AutoHostUtils.getPathFromId(AutoHostUtils.DEFAULT_PACK_ID);
    public static boolean generateWhenDisabled = false;
    public static OutputGenerator.Result mainPackResult;

    public static void init(MinecraftServer server) {
        var config = CommonImpl.loadConfig("auto-host", AutoHostConfig.class);
        AutoHost.config = config;

        if (!config.enabled) {
            if (generateWhenDisabled) {
                startResourcePackGeneration(server);
            }
            return;
        }

        try {
            AutoHost.message = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, AutoHost.config.message).getOrThrow().getFirst();
        } catch (Exception e) {
            AutoHost.message = null;
        }

        try {
            AutoHost.disconnectMessage = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, AutoHost.config.disconnectMessage).getOrThrow().getFirst();
        } catch (Exception e) {
            AutoHost.disconnectMessage = Component.translatable("multiplayer.texturePrompt.failure.line1");
        }

        try {
            AutoHost.dialogTitle = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, AutoHost.config.dialogTitle).getOrThrow().getFirst();
        } catch (Exception e) {
            AutoHost.dialogTitle = Component.literal("The server's resource pack is still generating");
        }

        try {
            AutoHost.dialogDefaultBody = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, AutoHost.config.dialogDefaultBody).getOrThrow().getFirst();
        } catch (Exception e) {
            AutoHost.dialogDefaultBody = Component.literal("Waiting...");
        }

        try {
            AutoHost.dialogHeader = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, AutoHost.config.dialogHeader).getOrThrow().getFirst();
        } catch (Exception e) {
            AutoHost.dialogHeader = Component.literal("This server requires a resource pack, which hasn't finished generating yet...\nIt might take a moment for it to finish!");
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
        startResourcePackGeneration(server);
    }

    public static void startResourcePackGeneration(MinecraftServer minecraftServer) {
        try {
            PolymerResourcePackMod.generateAndCall(minecraftServer, true, minecraftServer::sendSystemMessage, (_) -> {});
        } catch (Throwable e) {
            CommonImpl.LOGGER.warn("Failed to generate the resource pack!", e);
        }
    }

    public static void end(MinecraftServer server) {
        provider.serverStopped(server);
    }

    @Nullable
    public static Path getPath(String path) {
        var plus = path.indexOf('+');
        if (plus != -1) {
            path = path.substring(0, plus);
        }

        if (path.equals(DEFAULT_PATH)) {
            var mainPath = PolymerResourcePackUtils.getMainPath();
            if (PolymerResourcePackMod.useMainPath) {
                return mainPath;
            }

            return mainPath.resolveSibling(mainPath.getFileName().toString() + "_server.zip");
        }

        return FILES.get(path);
    }

    @Override
    public void onInitialize() {
        CommonImpl.registerConfig("auto-host", AutoHostConfig.class);

        ResourcePackDataProvider.register(Identifier.fromNamespaceAndPath("polymer", "automatic"), NettyProvider::new);
        ResourcePackDataProvider.register(Identifier.fromNamespaceAndPath("polymer", "auto"), NettyProvider::new);

        ResourcePackDataProvider.register(Identifier.fromNamespaceAndPath("polymer", "netty"), NettyProvider::new);
        ResourcePackDataProvider.register(Identifier.fromNamespaceAndPath("polymer", "same_port"), NettyProvider::new);
        ResourcePackDataProvider.register(Identifier.fromNamespaceAndPath("polymer", "http_server"), StandaloneWebServerProvider::new);
        ResourcePackDataProvider.register(Identifier.fromNamespaceAndPath("polymer", "standalone"), StandaloneWebServerProvider::new);
        ResourcePackDataProvider.register(Identifier.fromNamespaceAndPath("polymer", "external"), ExternalProvider::new);
        ResourcePackDataProvider.register(Identifier.fromNamespaceAndPath("polymer", "empty"), EmptyProvider::new);

        CommonImplUtils.registerCommands((c) -> c.then(literal("generate-pack")
                        .requires(CommonImplUtils.permission("command.generate", 3))
                        .then(literal("reload").executes((ctx -> {
                            return PolymerResourcePackMod.generateResources(ctx, (_) -> {
                                for (var player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                    if (provider.isReady(player.connection.getPacketContext())) {
                                        for (var x : provider.getProperties(player.connection.getPacketContext())) {
                                            player.connection.send(new ClientboundResourcePackPushPacket(x.id(), x.url(), x.hash(), AutoHost.config.require || PolymerResourcePackUtils.isRequired(), Optional.ofNullable(AutoHost.message)));
                                        }
                                    }
                                }
                            });
                        })))
                )
        );

        CommonImplUtils.registerDevCommands((c) -> {
            c.then(literal("reload_resourcepack").executes(context -> {
                if (provider.isReady(context.getSource().getPlayerOrException().connection.getPacketContext())) {
                    for (var x : provider.getProperties(context.getSource().getPlayerOrException().connection.getPacketContext())) {
                        context.getSource().getPlayerOrException().connection.send(new ClientboundResourcePackPushPacket(x.id(), x.url(), x.hash(), AutoHost.config.require || PolymerResourcePackUtils.isRequired(), Optional.ofNullable(AutoHost.message)));
                    }
                }
                return 0;
            }));
        });

        AutoHostUtils.SEND_RESOURCE_PACK_COLLECTOR.register(((provider1, context, consumer) -> {
            if (mainPackResult == null) return;
            consumer.accept(ResourcePackDataProvider.createProperties(PolymerResourcePackUtils.getMainUuid(), provider1.getMainFilePath(context), mainPackResult.hash()));
        }));

        AutoHostUtils.RESOURCE_PACKS_READY.register(((_, _) -> mainPackResult != null));

        PolymerResourcePackUtils.RESOURCE_PACK_INITIALIZED_EVENT.register(() -> {
            mainPackResult = null;
        });

        PolymerResourcePackUtils.RESOURCE_PACK_FINISHED_EVENT.register(v -> {
            if (v instanceof OutputGenerator.Result result) {
                mainPackResult = result;
            }
        });
    }
}
