package eu.pb4.polymer.resourcepack.impl.generation;

import com.google.gson.*;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PackResource;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.api.metadata.PackMcMeta;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.mixin.accessors.ResourceFilterAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class DefaultRPBuilder implements InternalRPBuilder {
    public static final Logger LOGGER = LoggerFactory.getLogger(DefaultRPBuilder.class);
    public static final Gson GSON = CommonImpl.GSON;
    public final SimpleEvent<Consumer<List<String>>> buildEvent = new SimpleEvent<>();
    private final HashMap<String, PackResource> fileMap = new HashMap<>();
    private final OutputGenerator outputGenerator;
    private final Set<ModContainer> modsList = new HashSet<>();
    private final Map<String, JsonArray> atlasDefinitions = new HashMap<>();
    private final Map<String, JsonObject> objectMergeDefinitions = new HashMap<>();
    private final List<Path> rootPaths = new ArrayList<>();
    private final List<ResourceConverter> converters = new ArrayList<>();
    private final Consumer<String> status;
    private boolean hasVanilla;
    private final PackMcMeta.Builder packMetadata = new PackMcMeta.Builder();
    private final List<Consumer<ResourcePackBuilder>> preFinishTask = new ArrayList<>();

    public DefaultRPBuilder(Path outputPath, Consumer<String> status) {
        try {
            Files.createDirectories(outputPath.getParent());
        } catch (Throwable e) {
            LOGGER.error("Couldn't create " + outputPath.getParent() + " directory!", e);
        }

        try {
            if (outputPath.toFile().exists()) {
                Files.deleteIfExists(outputPath);
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't remove " + outputPath + " file!", e);
        }
        this.outputGenerator = OutputGenerator.zipGenerator(outputPath);
        this.status = status;
    }
    public DefaultRPBuilder(OutputGenerator generator, Consumer<String> status) {
        this.status = status;
        this.outputGenerator = generator;
    }

    private static Path getSelfPath(String path) {
        return FabricLoader.getInstance().getModContainer("polymer-resource-pack").get().getPath(path);
    }

    @Override
    public boolean addData(String path, PackResource data) {
        try {
            if (path.endsWith(".json")) {
                if (path.equals("pack.mcmeta")) {
                    return this.addPackMcMeta(path, data.readAllBytes(), (x) -> {});
                }

                var split = path.split("/");

                if (split.length >= 3 && split[0].equals("assets")) {
                    if (split[2].equals("atlases")) {
                        return this.addAtlasFile(path, data.readAllBytes());
                    } else if (split[2].equals("lang")) {
                        return this.addMergedObjectFile(path, data.readAllBytes());
                    } else if (split[2].equals("sounds.json")) {
                        return this.addMergedSoundsFile(path, data.readAllBytes());
                    }
                }
            }

            this.fileMap.put(path, data);
            return true;
        } catch (Exception e) {
            if (PolymerResourcePackImpl.LOG_ERRORS) {
                LOGGER.warn("Something went wrong while adding raw data to path: " + path, e);
            }
            return false;
        }
    }

    private boolean addPackMcMeta(String path, byte[] data, Consumer<String> overlayConsumer) {
        try {
            var pack = PackMcMeta.fromString(new String(data, StandardCharsets.UTF_8));
            this.addPackMcMeta(pack, overlayConsumer);
            return true;
        } catch (Throwable e) {
            if (PolymerResourcePackImpl.LOG_ERRORS) {
                LOGGER.warn("Failed to load '{}'", path, e);
            }
        }


        return false;
    }

    private void addPackMcMeta(PackMcMeta pack, Consumer<String> overlayConsumer) {
        pack.filter().ifPresent(x -> ((ResourceFilterAccessor) x).getBlocks().forEach(this.packMetadata::addFilter));
        pack.overlays().ifPresent(x -> x.overlays().forEach((o) -> {
            overlayConsumer.accept(o.overlay());
            this.packMetadata.addOverlay(o);
        }));
        pack.language().ifPresent(x -> x.definitions().forEach(this.packMetadata::addLanguage));
    }

    private boolean addMergedObjectFile(String path, byte[] data) {
        try {
            var decode = JsonParser.parseString(new String(data, StandardCharsets.UTF_8));

            if (decode instanceof JsonObject obj) {
                var out = this.objectMergeDefinitions.computeIfAbsent(path, (x) -> new JsonObject());
                for (var key : obj.keySet()) {
                    out.add(key, obj.get(key));
                }
                return true;
            }
        } catch (Throwable e) {
            if (PolymerResourcePackImpl.LOG_ERRORS) {
                LOGGER.warn("Failed to parse merged object '{}'!", path, e);
            }
        }
        return false;
    }

    private boolean addMergedSoundsFile(String path, byte[] data) {
        try {
            var decode = JsonParser.parseString(new String(data, StandardCharsets.UTF_8));

            if (decode instanceof JsonObject obj) {
                var out = this.objectMergeDefinitions.computeIfAbsent(path, (x) -> new JsonObject());
                for (var key : obj.keySet()) {
                    var value = obj.getAsJsonObject(key);
                    if (!out.has(key) || (value.has("replace") && value.get("replace").getAsBoolean())) {
                        out.add(key, obj.get(key));
                        continue;
                    }
                    var existing = out.getAsJsonObject(key);
                    if (value.has("subtitle")) {
                        existing.add("subtitle", value.get("subtitle"));
                    }
                    if (value.has("sounds")) {
                        if (existing.get("sounds") instanceof JsonArray array) {
                            array.addAll(value.getAsJsonArray("sounds"));
                        } else {
                            existing.add("sounds", value.get("sounds"));
                        }
                    }
                }
                return true;
            }
        } catch (Throwable e) {
            if (PolymerResourcePackImpl.LOG_ERRORS) {
                LOGGER.warn("Failed to parse sound file '{}'!", path, e);
            }        }
        return false;
    }

    private boolean addAtlasFile(String path, byte[] data) {
        try {
            var decode = JsonParser.parseString(new String(data, StandardCharsets.UTF_8));

            if (decode instanceof JsonObject obj) {
                var list = obj.getAsJsonArray("sources");
                this.atlasDefinitions.computeIfAbsent(path, (x) -> new JsonArray()).addAll(list);
                return true;
            }
        } catch (Throwable e) {
            if (PolymerResourcePackImpl.LOG_ERRORS) {
                LOGGER.warn("Failed to parse atlas file '{}'!", path, e);
            }        }
        return false;
    }
    @Override
    public boolean copyFromPath(Path basePath, String targetPrefix, boolean override) {
        try {
            if (Files.isSymbolicLink(basePath)) {
                basePath = Files.readSymbolicLink(basePath);
            }

            if (Files.isDirectory(basePath)) {
                status.accept("action:copy_path_start/" + basePath);
                Path finalBasePath = basePath;
                try (var str = Files.walk(basePath)) {
                    str.forEach((file) -> {
                        var relative = finalBasePath.relativize(file);
                        var path = targetPrefix + relative.toString().replace("\\", "/");
                        if ((override || !fileMap.containsKey(path)) && Files.isRegularFile(file)) {
                            try {
                                this.addData(path, Files.readAllBytes(file));
                            } catch (IOException e) {
                                if (PolymerResourcePackImpl.LOG_ERRORS) {
                                    LOGGER.warn("Failed to load '{}'", path, e);
                                }
                            }

                        }
                    });
                }
                status.accept("action:copy_path_end/" + basePath);
                return true;
            } else if (Files.isRegularFile(basePath)) {
                try (var fs = FileSystems.newFileSystem(basePath, Collections.emptyMap())) {
                    fs.getRootDirectories().forEach((path) -> copyFromPath(path, targetPrefix, override));
                }
                return true;
            }
            status.accept("action:copy_path_failed/" + basePath);
            return false;
        } catch (Exception e) {
            LOGGER.error("Something went wrong while copying data from: " + basePath, e);
            status.accept("action:copy_path_failed/" + basePath);
            return false;
        }
    }

    @Override
    public boolean addModToCredits(String modId) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        if (mod.isPresent()) {
            ModContainer container = mod.get();
            this.modsList.add(container);
        }
        return mod.isPresent();
    }

    @Override
    public boolean copyAssets(String modId) {
        status.accept("action:copy_mod_start/" + modId);

        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        if (mod.isPresent()) {
            ModContainer container = mod.get();
            this.modsList.add(container);
            try {
                for (var rootPaths : container.getRootPaths()) {
                    try (var str = Files.list(rootPaths)) {
                        str.forEach(file -> {
                            try {
                                var name = file.getFileName().toString();
                                if (name.toLowerCase(Locale.ROOT).contains("license") || name.toLowerCase(Locale.ROOT).contains("licence")) {
                                    this.addData("licenses/" + modId + "/" + name, Files.readAllBytes(file));
                                }
                            } catch (Throwable ignored) {
                            }
                        });
                    } catch (Throwable e) {
                        LOGGER.warn("Failed while copying the license!", e);
                    }
                    var baseToCopy = new ArrayList<String>();
                    baseToCopy.add("assets");
                    try {
                        var packFile = rootPaths.resolve("pack.mcmeta");
                        if (Files.exists(packFile)) {
                            var pack = PackMcMeta.fromString(Files.readString(packFile));
                            this.addPackMcMeta(pack, baseToCopy::add);
                        }
                    } catch (Throwable ignored) {}

                    for (var x : baseToCopy) {
                        Path assets = rootPaths.resolve(x);
                        if (Files.exists(assets)) {
                            try (var str = Files.walk(assets)) {
                                str.forEach((file) -> {
                                    var relative = assets.relativize(file);
                                    var path = relative.toString().replace("\\", "/");
                                    if (Files.isRegularFile(file)) {
                                        try {
                                            this.addData(x + "/" + path, Files.readAllBytes(file));
                                        } catch (IOException e) {
                                            LOGGER.warn("Failed to load '{}'", assets + "/" + path, e);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
                status.accept("action:copy_mod_end/" + modId);

                return true;
            } catch (Exception e) {
                status.accept("action:copy_mod_fail/" + modId);
                LOGGER.error("Something went wrong while copying assets of mod: " + modId, e);
                return false;
            }
        }
        status.accept("action:copy_mod_fail/" + modId);
        LOGGER.warn("Tried to copy assets from non existing mod " + modId);
        return false;
    }

    @Override
    public byte[] getData(String path) {
        return this.fileMap.containsKey(path) ? this.fileMap.get(path).readAllBytes() : null    ;
    }

    @Override
    public @Nullable PackResource getResource(String path) {
        return this.fileMap.get(path);
    }

    @Override
    @Nullable
    public byte[] getDataOrSource(String path) {
        if (this.fileMap.containsKey(path)) {
            return this.fileMap.get(path).readAllBytes();
        } else {
            return this.getSourceData(path);
        }
    }

    @Override
    public void forEachResource(BiConsumer<String,  PackResource> consumer) {
        Map.copyOf(this.fileMap).forEach(consumer);
    }

    @Override
    public boolean addAssetsSource(String modId) {
        status.accept("action:add_source_mod_start/" + modId);
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            this.rootPaths.addAll(FabricLoader.getInstance().getModContainer(modId).get().getRootPaths());
            return true;
        }
        status.accept("action:add_source_mod_end/" + modId);

        return false;
    }

    @Override
    public void addResourceConverter(ResourceConverter converter) {
        this.converters.add(converter);
    }

    @Override
    public void addPreFinishTask(Consumer<ResourcePackBuilder> consumer) {
        this.preFinishTask.add(consumer);
    }

    @Nullable
    private byte[] getSourceData(String path) {
        try {
            var stream = getSourceStream(path);
            if (stream != null) {
                return stream.readAllBytes();
            }
        } catch (Throwable e) {
            LOGGER.warn("Error occurred while getting data from vanilla jar!", e);
        }
        return null;
    }

    @Nullable
    private InputStream getSourceStream(String path) {
        try {
            if (!this.hasVanilla && path.startsWith("assets/minecraft/")) {
                this.rootPaths.add(PolymerCommonUtils.getClientJarRoot());
                this.hasVanilla = true;
            }

            for (var rootPath : this.rootPaths) {
                var entry = rootPath.resolve(path);

                if (Files.exists(entry)) {
                    return Files.newInputStream(entry);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error occurred while getting data from vanilla jar!", e);
        }

        return null;
    }

    @Override
    public CompletableFuture<Boolean> buildResourcePack() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var credits = new ArrayList<String>();
                credits.add("");
                credits.add("  +-----------+");
                credits.add("  |           |");
                credits.add("  |   #   #   |");
                credits.add("  |           |");
                credits.add("  |   #   #   |");
                credits.add("  |    ###    |");
                credits.add("  |           |");
                credits.add("  |           |");
                credits.add("  +-----------+");
                credits.add("");
                credits.add("Generated with Polymer " + CommonImpl.VERSION);
                credits.add("");
                credits.add("Vanilla assets by Mojang Studios");
                credits.add("");
                credits.add("Contains assets from mods: ");

                status.accept("action:update_credits_start");
                var modsList = new ArrayList<>(this.modsList);
                modsList.sort(Comparator.comparing(x -> x.getMetadata().getId()));
                for (var entry : modsList) {
                    var b = new StringBuilder(" - ").append(entry.getMetadata().getName()).append(" (").append(entry.getMetadata().getId()).append(")");
                    if (!entry.getMetadata().getLicense().isEmpty()) {
                        b.append(" / License: ");
                        var iter = entry.getMetadata().getLicense().iterator();
                        while (iter.hasNext()) {
                            b.append(iter.next());
                            if (iter.hasNext()) {
                                b.append(", ");
                            }
                        }
                    }

                    entry.getMetadata().getContact().get("homepage").ifPresent(s -> b.append(" / Website: ").append(s));
                    entry.getMetadata().getContact().get("source").ifPresent(s -> b.append(" / Source: ").append(s));

                    credits.add(b.toString());
                }
                credits.add("");
                credits.add("See licenses folder for more information!");
                credits.add("");

                this.buildEvent.invoke((c) -> c.accept(credits));
                status.accept("action:update_credits_end");

                boolean bool = true;

                status.accept("action:merge_files_start");

                for (var entry : this.atlasDefinitions.entrySet()) {
                    var obj = new JsonObject();
                    obj.add("sources", entry.getValue());
                    this.fileMap.put(entry.getKey(), PackResource.of(obj.toString().getBytes(StandardCharsets.UTF_8)));
                }

                for (var entry : this.objectMergeDefinitions.entrySet()) {
                    this.fileMap.put(entry.getKey(), PackResource.of(entry.getValue().toString().getBytes(StandardCharsets.UTF_8)));
                }

                this.fileMap.put(AssetPaths.PACK_METADATA, PackResource.of(this.packMetadata.build().asString().getBytes(StandardCharsets.UTF_8)));
                status.accept("action:merge_files_end");


                if (!this.fileMap.containsKey(AssetPaths.PACK_ICON)) {
                    var filePath = FabricLoader.getInstance().getGameDir().resolve("server-icon.png");

                    if (filePath.toFile().exists()) {
                        this.fileMap.put(AssetPaths.PACK_ICON, PackResource.of(Files.readAllBytes(filePath)));
                    } else {
                        this.fileMap.put(AssetPaths.PACK_ICON, PackResource.of(Files.readAllBytes(getSelfPath("assets/icon.png"))));
                    }
                }

                this.fileMap.put("polymer-credits.txt", PackResource.of(String.join("\n", credits).getBytes(StandardCharsets.UTF_8)));


                status.accept("action:pre_finish_task_start");
                for (var task : this.preFinishTask) {
                    task.accept(this);
                }
                status.accept("action:pre_finish_task_end");


                status.accept("action:sort_files_start");
                for (var path : this.fileMap.keySet().toArray(new String[0])) {
                    var split = new ArrayList<>(List.of(path.split("/")));
                    while (split.size() > 1) {
                        split.removeLast();
                        this.fileMap.put(String.join("/", split) + "/", null);
                    }
                }
                var sorted = new ArrayList<>(this.fileMap.entrySet());
                sorted.sort(Map.Entry.comparingByKey());
                status.accept("action:sort_files_end");

                bool &= this.outputGenerator.generateFile(sorted, this::convertResource, status);

                return bool;
            } catch (Exception e) {
                LOGGER.error("Something went wrong while creating resource pack!", e);
                return false;
            }
        });
    }

    @Override
    public PackMcMeta.Builder getPackMcMetaBuilder() {
        return this.packMetadata;
    }

    @Nullable
    private PackResource convertResource(String path, PackResource resource) {
        if (this.converters.isEmpty()) {
            return resource;
        }
        for (var conv : this.converters) {
            resource = conv.convert(path, resource   );
            if (resource == null) {
                return null;
            }
        }

        return resource;
    }

    public interface OutputGenerator {
        boolean generateFile(List<Map.Entry<String, PackResource>> resources, ResourceConverter converter, Consumer<String> status);

        static OutputGenerator zipGenerator(Path out) {
            return (a, b, c) -> writeSingleZip(out, a, b, c);
        }
    }

    private static boolean writeSingleZip(Path out, Collection<Map.Entry<String, PackResource>> resources, ResourceConverter converter, Consumer<String> status) {
        status.accept("action:write_zip_start");

        try (var outputStream = new ZipOutputStream(Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            for (var entry : resources) {
                var path = entry.getKey();
                var resource = entry.getValue();

                if (resource != null) {
                    resource = converter.convert(path, resource);
                    if (resource == null) {
                        continue;
                    }
                }

                var zipEntry = new ZipEntry(path);
                zipEntry.setTime(0);
                outputStream.putNextEntry(zipEntry);
                if (resource != null) {
                    resource.getStream().transferTo(outputStream);
                }
                outputStream.closeEntry();
            }
        } catch (Throwable e) {
            status.accept("action:write_zip_fail");
            LOGGER.warn("Failed to write the zip file!", e);
            return false;
        }
        status.accept("action:write_zip_end");
        return true;
    }
}
