package eu.pb4.polymer.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.compat.polymc.PolyMcHelpers;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var command = literal("polymer")
                .requires((source) -> source.hasPermissionLevel(3))
                .then(literal("generate")
                        .executes(Commands::generate));

        if (PolymerGlobalValues.DEVELOPER_MODE) {
            command.then(literal("dev")
                    .then(literal("item-client")
                            .executes(Commands::itemClient))
                    .then(literal("reload-world")
                            .executes((ctx) -> {
                                PolymerUtils.reloadWorld(ctx.getSource().getPlayer());
                                return 0;
                            }))
                    .then(literal("protocol-info")
                            .executes((ctx) -> {
                                ctx.getSource().sendFeedback(new LiteralText("Protocol supported by your client:"), false);
                                for (var entry : PolymerNetworkHandlerExtension.of(ctx.getSource().getPlayer().networkHandler).polymer_getSupportMap().object2IntEntrySet()) {
                                    ctx.getSource().sendFeedback(new LiteralText("- " + entry.getKey() + " = " + entry.getIntValue()), false);
                                }
                                return 0;
                            }))

                    .then(literal("set-pack-status")
                            .then(argument("status", BoolArgumentType.bool())
                                    .executes((ctx) -> {
                                        var status = ctx.getArgument("status", Boolean.class).booleanValue();
                                        PolymerRPUtils.setPlayerStatus(ctx.getSource().getPlayer(), status);
                                        ctx.getSource().sendFeedback(new LiteralText("New resource pack status: " + status), false);
                                        return 0;
                                    })))
                    .then(literal("get-pack-status")
                                    .executes((ctx) -> {
                                        var status = PolymerRPUtils.hasPack(ctx.getSource().getPlayer());
                                        ctx.getSource().sendFeedback(new LiteralText("Resource pack status: " + status), false);
                                        return 0;
                                    })));
        }

        dispatcher.register(command);
    }

    private static int itemClient(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();
        var itemStack = player.getMainHandStack();

        context.getSource().sendFeedback(new LiteralText(PolymerItemUtils.getPolymerItemStack(itemStack, player).getOrCreateNbt().toString()), false);

        return 1;
    }

    public static int generate(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText("Starting resource pack generation..."), true);

        try {
            if (CompatStatus.POLYMC) {
                Path path = FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack-input");
                path.toFile().mkdirs();
                PolyMcHelpers.createResources(path);
            }
        } catch (Exception e) {
            // noop
        }

        boolean success = PolymerRPUtils.build(FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack.zip"));

        if (success) {
            context.getSource().sendFeedback(new LiteralText("Resource pack created successfully! You can find it in game folder as polymer-resourcepack.zip"), true);
        } else {
            context.getSource().sendError(new LiteralText("Found issues while creating resource pack! See logs above for more detail!"));
        }

        return 0;
    }
}
