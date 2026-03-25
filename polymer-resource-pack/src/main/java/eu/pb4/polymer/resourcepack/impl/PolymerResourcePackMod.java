package eu.pb4.polymer.resourcepack.impl;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.resourcepack.api.OutputGenerator;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackStatusConsumer;
import eu.pb4.polymer.resourcepack.api.metadata.PackMcMeta;
import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static net.minecraft.commands.Commands.literal;


@ApiStatus.Internal
public class PolymerResourcePackMod implements ModInitializer, ClientModInitializer {
	public static boolean alreadyGeneration = false;
    public static final List<String> STATUS = new CopyOnWriteArrayList<>();
    public static boolean useMainPath = true;

    @Override
	public void onInitialize() {
		CommonImplUtils.registerCommands((x) -> x.then(literal("generate-pack")
				.requires(CommonImplUtils.permission("command.generate", 3))
				.executes(PolymerResourcePackMod::generateResources)));

        for (var mod : FabricLoader.getInstance().getAllMods()) {
            var include = mod.getMetadata().getCustomValue("polymer:resource_pack_include");
            var require = mod.getMetadata().getCustomValue("polymer:resource_pack_require");

            if (include != null && include.getType() == CustomValue.CvType.BOOLEAN && include.getAsBoolean()) {
                PolymerResourcePackUtils.addModAssets(mod.getMetadata().getId());
            }

            if (require != null && require.getType() == CustomValue.CvType.BOOLEAN && require.getAsBoolean()) {
                PolymerResourcePackUtils.markAsRequired();
            }
        }

        var creator = PolymerResourcePackUtils.getInstance();
        creator.creationEvent.register((builder) -> {
            var config = PolymerResourcePackImpl.loadConfig();

            if (!config.preventedPaths.isEmpty()) {
                builder.addResourceConverter((path, data) -> {
                    for (var test : config.preventedPaths) {
                        if (path.startsWith(test)) {
                            return null;
                        }
                    }
                    return data;
                });
            }

            Path path = CommonImpl.getGameDir().resolve("polymer/source_assets");
            if (Files.isDirectory(path)) {
                builder.copyFromPath(path);
                try {
                    var metafile = path.resolve("pack.mcmeta");
                    if (Files.exists(metafile)) {
                        var meta = PackMcMeta.fromString(Files.readString(metafile));
                        builder.getPackMcMetaBuilder().metadata(meta.pack());
                    } else if (PolymerResourcePackImpl.IGNORE_PACK_VERSION) {
                        var og = builder.getPackMcMetaBuilder().metadata();
                        builder.getPackMcMetaBuilder().metadata(new PackMetadataSection(og.description(), new InclusiveRange<>(
                                SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES),
                                new PackFormat(Integer.MAX_VALUE, Integer.MAX_VALUE)
                        )));
                    }
                } catch (Throwable ignored) {}
            }

            try {
                for (var field : config.includeModAssets) {
                    builder.copyAssets(field);
                }
                var gamePath = FabricLoader.getInstance().getGameDir();

                Consumer<Path> zipReader = (zipPath) -> {
                    if (Files.exists(zipPath)) {
                        try (var fs = FileSystems.newFileSystem(zipPath)) {
                            for (var root : fs.getRootDirectories()) {
                                builder.copyResourcePackFromPath(root, zipPath.getFileName().toString());
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                };

                for (var field : config.includeZips) {
                    var parts = field.split("/");
                    if (parts.length == 0) continue;

                    if (parts[parts.length - 1].contains("*")) {
                        var folderPath = gamePath.resolve(String.join("/", Arrays.copyOfRange(parts, 0, parts.length - 1)));
                        if (!Files.isDirectory(folderPath)) {
                            continue;
                        }
                        try (var stream = Files.newDirectoryStream(folderPath, parts[parts.length - 1])){
                            stream.forEach(zipReader);
                        }
                    } else {
                        zipReader.accept(gamePath.resolve(field));
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        creator.afterInitialCreationEvent.register((builder) -> {
            Path path = CommonImpl.getGameDir().resolve("polymer/override_assets");
            if (Files.isDirectory(path)) {
                builder.copyFromPath(path);
                try {
                    var metafile = path.resolve("pack.mcmeta");
                    if (Files.exists(metafile)) {
                        var meta = PackMcMeta.fromString(Files.readString(metafile));
                        builder.getPackMcMetaBuilder().metadata(meta.pack());
                    }
                } catch (Throwable ignored) {}
            }
        });

	}

	@Override
	public void onInitializeClient() {
        CompletableFuture.runAsync(PolymerResourcePack::setup);
	}

	public static int generateResources(CommandContext<CommandSourceStack> context) {
        generateAndCall(context.getSource().getServer(), false, x -> context.getSource().sendSuccess(() -> x, true), (_) -> {});
        return 1;
    }

    public static int generateResources(CommandContext<CommandSourceStack> context, Consumer<OutputGenerator.Result> runnable) {
        generateAndCall(context.getSource().getServer(), false, x -> context.getSource().sendSuccess(() -> x, true), runnable);
        return 1;
    }

    public static void generateAndCall(MinecraftServer server, boolean ignoreLock, Consumer<Component> messageConsumer, Consumer<OutputGenerator.Result> runnable) {
        if (alreadyGeneration && !ignoreLock) {
            messageConsumer.accept(Component.literal("[Polymer] Pack is already generating! Wait for it to finish..."));
            return;
        }
        alreadyGeneration = true;

        Util.ioPool().execute(() -> {
            boolean success = false;
            OutputGenerator.Result result = null;
            try {
                messageConsumer.accept(Component.literal("[Polymer] Starting resource pack generation..."));
                STATUS.clear();
                Path outputPath = PolymerResourcePackUtils.getMainPath();
                if (Files.exists(outputPath)) {
                    try {
                        Files.delete(outputPath);
                        useMainPath = true;
                    } catch (Throwable e) {
                        // Failed to remove, change path to workaround one!
                        outputPath = outputPath.resolveSibling(outputPath.getFileName().toString() + "_server.zip");
                        try {
                            Files.delete(outputPath);
                            useMainPath = false;
                        } catch (Throwable f2) {
                            // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            // I hate windows
                        }
                    }
                }
                result = PolymerResourcePackUtils.getInstance().build(outputPath, ResourcePackStatusConsumer.simple(STATUS::add));
                success = result != null;

                STATUS.clear();
                final var finalOutputPath = outputPath;
                boolean finalSuccess = success;
                OutputGenerator.Result finalResult = result;
                server.execute(() -> {
                    alreadyGeneration = false;
                    if (finalSuccess) {
                        messageConsumer.accept(Component.literal("[Polymer] Resource pack created successfully! You can find it in game folder as ")
                                .append(Component.literal(PolymerResourcePackImpl.FILE_NAME)
                                        .setStyle(Style.EMPTY.withUnderlined(true)
                                                .withHoverEvent(new HoverEvent.ShowText(
                                                        Component.literal(finalOutputPath.toAbsolutePath().toString())))))
                        );
                        runnable.accept(finalResult);
                    } else {
                        messageConsumer.accept(Component.literal("[Polymer] Found issues while creating resource pack! See logs above for more detail!").withStyle(ChatFormatting.RED));
                    }
                });
            } catch (Throwable e) {
                messageConsumer.accept(Component.literal("[Polymer] Found critical issues while creating resource pack! See logs above for more detail!").withStyle(ChatFormatting.RED));
                CommonImpl.LOGGER.error("Failed to generate the resource pack!", e);
                alreadyGeneration = false;
            }
        });
    }
}
