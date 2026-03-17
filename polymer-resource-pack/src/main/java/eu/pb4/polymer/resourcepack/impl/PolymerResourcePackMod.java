package eu.pb4.polymer.resourcepack.impl;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.resourcepack.api.OutputGenerator;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackStatusConsumer;
import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Files;
import java.nio.file.Path;
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
                    result = PolymerResourcePackUtils.getInstance().build(outputPath, ResourcePackStatusConsumer.simple(STATUS::add));
                    success = result != null;
                }

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
