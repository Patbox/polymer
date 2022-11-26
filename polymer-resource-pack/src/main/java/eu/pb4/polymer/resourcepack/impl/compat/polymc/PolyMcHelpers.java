package eu.pb4.polymer.resourcepack.impl.compat.polymc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.PolymerRPBuilder;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.ApiStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
@SuppressWarnings({"deprecation", "unchecked"})
public class PolyMcHelpers {
    public static void importPolyMcResources(PolymerRPBuilder builder) {
        // Generate PolyMc's resource pack
        var pack = io.github.theepicblock.polymc.PolyMc.getMapForResourceGen().generateResourcePack(io.github.theepicblock.polymc.PolyMc.LOGGER);
        if (pack == null) return;

        // Directly write each of PolyMc's assets to a byte array in memory. This prevents the need to write it to disk
        pack.forEachAsset((namespace, path, asset) -> {
            try {
                // Write regular file
                var data = new ByteArrayOutputStream();
                asset.writeToStream(data, pack.getGson());
                builder.addData(String.format("assets/%s/%s", namespace, path), data.toByteArray());

                // (optionally) write metafile
                asset.writeMetaToStream(() -> new PolymerRPOutputStream(String.format("assets/%s/%s.mcmeta", namespace, path), builder), pack.getGson());
            } catch (IOException e) {
                CommonImpl.LOGGER.error("Error importing " + namespace + ":" + path + " from PolyMc");
                e.printStackTrace();
            }
        });
    }


    public static void overrideCommand(MinecraftServer server) {
        var dispatcher = server.getCommandManager().getDispatcher();
        var generateNode
                = dispatcher.findNode(List.of("polymc", "generate")).createBuilder();

        dispatcher.register((LiteralArgumentBuilder<ServerCommandSource>) dispatcher.findNode(List.of("polymc"))
                .createBuilder().then(generateNode.then(literal("resources").executes(PolymerResourcePackMod::generateResources)))
        );
    }

    public static class PolymerRPOutputStream extends ByteArrayOutputStream {
        private final String path;
        private final PolymerRPBuilder builder;

        public PolymerRPOutputStream(String path, PolymerRPBuilder builder) {
            this.path = path;
            this.builder = builder;
        }

        @Override
        public void close() throws IOException {
            builder.addData(path, this.toByteArray());
            super.close();
        }
    }
}
