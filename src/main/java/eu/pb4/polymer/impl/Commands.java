package eu.pb4.polymer.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.impl.compat.polymc.PolyMcHelpers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
public class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("polymer")
                .requires((source) -> source.hasPermissionLevel(3))
                .then(literal("generate")
                        .executes(Commands::generate))
        );
    }

    public static int generate(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText("Starting resource pack generation..."), true);

        try {
            if (PolymerMod.POLYMC_COMPAT) {
                Path path = FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack-input");
                path.toFile().mkdirs();
                PolyMcHelpers.createResources(path);
            }
        } catch (Exception e) {}

        boolean success = PolymerRPUtils.build(FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack.zip"));

        if (success) {
            context.getSource().sendFeedback(new LiteralText("Resource pack created successfully! You can find it in game foleder as polymer-resourcepack.zip"), true);
        } else {
            context.getSource().sendError(new LiteralText("Found issues while creating resource pack! See logs above for more detail!"));
        }

        return 0;
    }
}
