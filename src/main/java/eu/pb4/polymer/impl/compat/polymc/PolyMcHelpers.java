package eu.pb4.polymer.impl.compat.polymc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pb4.polymer.impl.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
public class PolyMcHelpers {
    public static void createResources(Path path) {
        Path inputPath = path.resolve("input");
        inputPath.toFile().mkdirs();

        io.github.theepicblock.polymc.impl.resource.ResourcePackGenerator.generate(
                io.github.theepicblock.polymc.PolyMc.getMainMap(),
                inputPath.toString(),
                new io.github.theepicblock.polymc.impl.misc.logging.ErrorTrackerWrapper(
                        io.github.theepicblock.polymc.PolyMc.LOGGER)
        );
    }

    public static void overrideCommand(MinecraftServer server) {
        var dispatcher = server.getCommandManager().getDispatcher();
        var generateNode
                = dispatcher.findNode(List.of("polymc", "generate")).createBuilder();

        dispatcher.register((LiteralArgumentBuilder<ServerCommandSource>) dispatcher.findNode(List.of("polymc"))
                .createBuilder().then(generateNode.then(literal("resources").executes(Commands::generate)))
        );
    }
}
