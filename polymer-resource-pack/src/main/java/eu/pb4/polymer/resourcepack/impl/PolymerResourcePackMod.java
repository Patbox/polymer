package eu.pb4.polymer.resourcepack.impl;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import eu.pb4.polymer.resourcepack.impl.compat.polymc.PolyMcHelpers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.literal;


@ApiStatus.Internal
public class PolymerResourcePackMod implements ModInitializer, ClientModInitializer {
	public static boolean alreadyGeneration = false;
	@Override
	public void onInitialize() {
		if (CompatStatus.POLYMC) {
			PolymerResourcePackUtils.markAsRequired();
			ServerLifecycleEvents.SERVER_STARTED.register(PolyMcHelpers::overrideCommand);
		}

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
		PolymerResourcePack.setup();
	}

	public static int generateResources(CommandContext<ServerCommandSource> context) {
        generateAndCall(context.getSource().getServer(), false, x -> context.getSource().sendFeedback(() -> x, true), () -> {});
        return 1;
    }

    public static int generateResources(CommandContext<ServerCommandSource> context, Runnable runnable) {
        generateAndCall(context.getSource().getServer(), false, x -> context.getSource().sendFeedback(() -> x, true), runnable);
        return 1;
    }

    public static void generateAndCall(MinecraftServer server, boolean ignoreLock, Consumer<Text> messageConsumer, Runnable runnable) {
        if (alreadyGeneration && !ignoreLock) {
            messageConsumer.accept(Text.literal("[Polymer] Pack is already generating! Wait for it to finish..."));
            return;
        }
        alreadyGeneration = true;

        Util.getIoWorkerExecutor().execute(() -> {
            try {
                messageConsumer.accept(Text.literal("[Polymer] Starting resource pack generation..."));
                boolean success = PolymerResourcePackUtils.buildMain();

                server.execute(() -> {
                    alreadyGeneration = false;
                    if (success) {
                        messageConsumer.accept(Text.literal("[Polymer] Resource pack created successfully! You can find it in game folder as ")
                                .append(Text.literal(PolymerResourcePackImpl.FILE_NAME)
                                        .setStyle(Style.EMPTY.withUnderline(true)
                                                .withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        Text.literal(PolymerResourcePackUtils.getMainPath().toAbsolutePath().toString())))))
                        );
                        runnable.run();
                    } else {
                        messageConsumer.accept(Text.literal("[Polymer] Found issues while creating resource pack! See logs above for more detail!").formatted(Formatting.RED));
                    }
                });
            } catch (Throwable e) {
                messageConsumer.accept(Text.literal("[Polymer] Found critical issues while creating resource pack! See logs above for more detail!").formatted(Formatting.RED));
                CommonImpl.LOGGER.error("Failed to generate the resource pack!", e);
                alreadyGeneration = false;
            }
        });
    }
}
