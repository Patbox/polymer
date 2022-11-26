package eu.pb4.polymer.resourcepack.impl;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.impl.client.rendering.PolymerResourcePack;
import eu.pb4.polymer.resourcepack.impl.compat.polymc.PolyMcHelpers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

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
	}

	@Override
	public void onInitializeClient() {
		PolymerResourcePack.setup();
	}

	public static int generateResources(CommandContext<ServerCommandSource> context) {
        if (alreadyGeneration) {
            context.getSource().sendFeedback(Text.literal("Pack is already generating! Wait for it to finish..."), true);
        } else {
            alreadyGeneration = true;

            Util.getIoWorkerExecutor().execute(() -> {
                context.getSource().sendFeedback(Text.literal("Starting resource pack generation..."), true);
                boolean success = PolymerResourcePackUtils.build();

                context.getSource().getServer().execute(() -> {
                    if (success) {
                        context.getSource().sendFeedback(
                                Text.literal("Resource pack created successfully! You can find it in game folder as ")
                                .append(Text.literal("polymer-resourcepack.zip")
                                        .setStyle(Style.EMPTY.withUnderline(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(PolymerResourcePackUtils.DEFAULT_PATH.toAbsolutePath().toString()))))),
                                true
                        );
                    } else {
                        context.getSource().sendError(Text.literal("Found issues while creating resource pack! See logs above for more detail!"));
                    }
                });

                alreadyGeneration = false;
            });
        }
		return 0;
	}
}
