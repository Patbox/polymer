package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.resourcepack.api.metadata.PackMcMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@ApiStatus.NonExtendable
public interface ResourcePackBuilder {
    boolean addData(String path, byte[] data);

    boolean copyAssets(String modId);

    boolean copyFromPath(Path path, String targetPrefix, boolean override);

    default boolean copyFromPath(Path path, String targetPrefix) {
        return this.copyFromPath(path, targetPrefix, true);
    }

    default boolean copyFromPath(Path path) {
        return this.copyFromPath(path, "", true);
    }

    default boolean copyFromPath(Path path, boolean override) {
        return this.copyFromPath(path, "", override);
    }

    default boolean copyResourcePackFromPath(Path root) {
        return copyResourcePackFromPath(root, "__undefined__");
    }

    default boolean copyResourcePackFromPath(Path root, String field) {
        try {
            {
                var assets = root.resolve("assets");
                if (Files.exists(assets)) {
                    copyFromPath(assets, "assets/");
                }
            }

            var packmcmeta = root.resolve("pack.mcmeta");
            if (Files.exists(packmcmeta)) {
                try {
                    var str = Files.readString(packmcmeta);
                    this.addData("pack.mcmeta", str.getBytes(StandardCharsets.UTF_8));

                    var pack = PackMcMeta.fromString(str);
                    if (pack.overlays().isPresent()) {
                        for (var ov : pack.overlays().get().overlays()) {
                            var assets = root.resolve(ov.overlay());
                            if (Files.exists(assets)) {
                                copyFromPath(assets, ov.overlay() + "/");
                            }
                        }
                    }

                } catch (Throwable ignored) {}
            }

            try (var str = Files.list(root)) {
                str.forEach(file -> {
                    try {
                        var name = file.getFileName().toString();
                        if (name.toLowerCase(Locale.ROOT).contains("license")
                                || name.toLowerCase(Locale.ROOT).contains("licence")) {
                            this.addData("licenses/"
                                    + field.replace("/", "_").replace("\\", "_") + "/" + name, Files.readAllBytes(file));
                        }
                    } catch (Throwable ignored) {
                    }
                });
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    byte @Nullable [] getData(String path);

    byte @Nullable [] getDataOrSource(String path);

    void forEachFile(BiConsumer<String, byte[]> consumer);

    boolean addAssetsSource(String modId);

    void addWriteConverter(BiFunction<String, byte[], byte  @Nullable []> converter);

    void addPreFinishTask(Consumer<ResourcePackBuilder> consumer);

    default PackMcMeta.Builder getPackMcMetaBuilder() {
        return new PackMcMeta.Builder();
    }
}
