package eu.pb4.polymer.resourcepack.impl.client.rendering;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class PolymerResourcePack {
    @Nullable
    private volatile static Path path = null;

    public synchronized static Pack.@Nullable ResourcesSupplier setup() {
        if (path != null && Files.exists(path)) {
            return new FilePackResources.FileResourcesSupplier(path);
        }

        Path outputPath = PolymerResourcePackUtils.getMainPath();
        if (Files.exists(outputPath)) {
            try {
                Files.delete(outputPath);
            } catch (Throwable e) {
                // Failed to remove, change path to workaround one!
                outputPath = outputPath.resolveSibling(outputPath.getFileName().toString() + "_client.zip");
                if (Files.exists(outputPath)) {
                    try {
                        Files.delete(outputPath);
                    } catch (Throwable f2) {
                        // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                        // I hate windows
                    }
                }
            }
        }

        try {
            if (PolymerResourcePackUtils.getInstance().build(outputPath) != null) {
                path = outputPath;
                return new FilePackResources.FileResourcesSupplier(outputPath);
            }
        } catch (Throwable e) {

        }

        return null;
    }

    public static class Provider implements RepositorySource {
        @Override
        public void loadPacks(Consumer<Pack> profileAdder) {
            if (PolymerResourcePackUtils.hasResources()) {
                var pack = PolymerResourcePack.setup();

                if (pack != null) {
                    profileAdder.accept(Pack.readMetaAndCreate(
                            new PackLocationInfo(ClientUtils.PACK_ID,
                            Component.translatable("text.polymer.resource_pack.name"), PackSource.BUILT_IN, Optional.empty()),
                            pack,
                            PackType.CLIENT_RESOURCES,
                            new PackSelectionConfig(PolymerResourcePackUtils.isRequired(), Pack.Position.TOP, false)
                    ));
                }
            }
        }
    }
}
