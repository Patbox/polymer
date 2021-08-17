package eu.pb4.polymer.other.polymc;

import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

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
}
