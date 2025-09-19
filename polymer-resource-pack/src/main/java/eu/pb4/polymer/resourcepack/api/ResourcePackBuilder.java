package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.resourcepack.api.metadata.PackMcMeta;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@ApiStatus.NonExtendable
public interface ResourcePackBuilder {
    default boolean addData(String path, byte[] data) {
        return this.addData(path, PackResource.of(data));
    }
    boolean addData(String path, PackResource resource);
    default boolean addData(String path, WritableAsset data) {
        return this.addData(path, data.toBytes());
    }
    default boolean addStringData(String path, String data) {
        return addData(path, data.getBytes(StandardCharsets.UTF_8));
    }

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
                                    + field.replace("/", "_").replace("\\", "_") + "/" + name, PackResource.of(file));
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
    @Nullable
    PackResource getResource(String path);

    byte @Nullable [] getDataOrSource(String path);

    default @Nullable String getStringData(String path) {
        var data = getData(path);
        return data != null ? new String(data, StandardCharsets.UTF_8) : null;
    }

    default @Nullable String getStringDataOrSource(String path) {
        var data = getDataOrSource(path);
        return data != null ? new String(data, StandardCharsets.UTF_8) : null;
    }

    @Deprecated(forRemoval = true)
    default void forEachFile(BiConsumer<String, byte[]> consumer) {
        forEachResource((a, b) -> consumer.accept(a, b.readAllBytes()));
    }
    void forEachResource(BiConsumer<String, PackResource> consumer);

    boolean addAssetsSource(String modId);

    void addResourceConverter(ResourceConverter converter);

    @Deprecated(forRemoval = true)
    default void addWriteConverter(BiFunction<String, byte[], byte @Nullable []> converter) {
        this.addResourceConverter((path, data) -> {
            var t = converter.apply(path, data.readAllBytes());
            return t != null ? PackResource.of(t) : null;
        });
    }

    void addPreFinishTask(Consumer<ResourcePackBuilder> consumer);

    default PackMcMeta.Builder getPackMcMetaBuilder() {
        return new PackMcMeta.Builder();
    }

    default boolean addModToCredits(String modId) {
        return false;
    }

    interface ResourceConverter {
        @Nullable
        PackResource convert(String path, PackResource resource);
    }

    interface OutputGenerator {
        boolean generateFile(List<Map.Entry<String, PackResource>> resources, ResourceConverter converter, Consumer<String> status);

        static OutputGenerator zipGenerator(Path out) {
            return (a, b, c) -> DefaultRPBuilder.writeSingleZip(out, a, b, c);
        }
    }
}
