package eu.pb4.polymer.resourcepack.impl.generation;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.impl.ArmorTextureMetadata;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static eu.pb4.polymer.resourcepack.api.AssetPaths.armorOverlayTexture;
import static eu.pb4.polymer.resourcepack.api.AssetPaths.armorTexture;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class DefaultRPBuilder implements InternalRPBuilder {
    public static final Gson GSON = CommonImpl.GSON;
    public final SimpleEvent<Consumer<List<String>>> buildEvent = new SimpleEvent<>();
    private final Map<Item, JsonArray[]> customModels = new HashMap<>();
    private final TreeMap<String, byte[]> fileMap = new TreeMap<>();
    private final List<PolymerArmorModel> armors = new ArrayList<>();
    private final Path outputPath;
    private final List<ModContainer> modsList = new ArrayList<>();
    private final Map<Identifier, List<PolymerModelData>> customModelData = new HashMap<>();
    private final Map<String, JsonArray> atlasDefinitions = new HashMap<>();
    private final List<Path> rootPaths = new ArrayList<>();
    private final List<BiFunction<String, byte[], @Nullable byte[]>> converters = new ArrayList<>();
    private boolean hasVanilla;

    public DefaultRPBuilder(Path outputPath) {
        try {
            Files.createDirectories(outputPath.getParent());
        } catch (Throwable e) {
            CommonImpl.LOGGER.warn("Couldn't create " + outputPath.getParent() + " directory!");
            e.printStackTrace();
        }
        this.outputPath = outputPath;

        try {
            if (outputPath.toFile().exists()) {
                Files.deleteIfExists(outputPath);
            }
        } catch (Exception e) {
            CommonImpl.LOGGER.warn("Couldn't remove " + outputPath + " file!");
            e.printStackTrace();
        }

    }

    private static Path getSelfPath(String path) {
        return FabricLoader.getInstance().getModContainer("polymer-resource-pack").get().getPath(path);
    }

    @Override
    public boolean addData(String path, byte[] data) {
        try {
            if (path.endsWith(".json")) {
                var split = path.split("/");

                if (split.length > 3 && split[0].equals("assets") && split[2].equals("atlases")) {
                    return this.addAtlasFile(path, data);
                }
            }

            this.fileMap.put(path, data);
            return true;
        } catch (Exception e) {
            CommonImpl.LOGGER.error("Something went wrong while adding raw data to path: " + path);
            e.printStackTrace();
            return false;
        }
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
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean copyFromPath(Path basePath, String targetPrefix, boolean override) {
        try {
            if (Files.isSymbolicLink(basePath)) {
                basePath = Files.readSymbolicLink(basePath);
            }

            if (Files.isDirectory(basePath)) {
                Path finalBasePath = basePath;
                try (var str = Files.walk(basePath)) {
                    str.forEach((file) -> {
                        var relative = finalBasePath.relativize(file);
                        var path = targetPrefix + relative.toString().replace("\\", "/");
                        if ((override || !fileMap.containsKey(path)) && Files.isRegularFile(file)) {
                            try {
                                this.addData(path, Files.readAllBytes(file));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }

                return true;
            } else if (Files.isRegularFile(basePath)) {
                try (var fs = FileSystems.newFileSystem(basePath, Collections.emptyMap())) {
                    fs.getRootDirectories().forEach((path) -> copyFromPath(path, targetPrefix, override));
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            CommonImpl.LOGGER.error("Something went wrong while copying data from: " + basePath, e);
            return false;
        }
    }

    @Override
    public boolean copyAssets(String modId) {
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
                                if (name.toLowerCase(Locale.ROOT).contains("license")
                                        || name.toLowerCase(Locale.ROOT).contains("licence")) {
                                    this.addData("licenses/"
                                            + modId + "/" + name, Files.readAllBytes(file));
                                }
                            } catch (Throwable ignored) {
                            }
                        });
                    } catch (Throwable e) {
                        CommonImpl.LOGGER.warn("Failed while copying the license!", e);
                    }

                    Path assets = rootPaths.resolve("assets");
                    if (Files.exists(assets)) {
                        try(var str = Files.walk(assets))  {
                            str.forEach((file) -> {
                                var relative = assets.relativize(file);
                                var path = relative.toString().replace("\\", "/");
                                if (Files.isRegularFile(file)) {
                                    try {
                                        this.addData("assets/" + path, Files.readAllBytes(file));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                CommonImpl.LOGGER.error("Something went wrong while copying assets of mod: " + modId);
                e.printStackTrace();
                return false;
            }
        }
        CommonImpl.LOGGER.warn("Tried to copy assets from non existing mod " + modId);
        return false;
    }

    @Override
    public boolean addCustomModelData(PolymerModelData cmdInfo) {
        try {
            JsonArray jsonArray = this.getCustomModels(cmdInfo.item(), OverridePlace.CUSTOM_MODEL_DATA);

            this.customModelData.computeIfAbsent(Registries.ITEM.getId(cmdInfo.item()), (x) -> new ArrayList<>()).add(cmdInfo);

            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("model", CommonImplUtils.shortId(cmdInfo.modelPath()));
                JsonObject predicateObject = new JsonObject();
                predicateObject.addProperty("custom_model_data", cmdInfo.value());
                jsonObject.add("predicate", predicateObject);

                jsonArray.add(jsonObject);
            }
            JsonObject modelObject = null;
            var modelPath = AssetPaths.model(cmdInfo.modelPath().getNamespace(), cmdInfo.modelPath().getPath() + ".json");

            if (this.fileMap.containsKey(modelPath)) {
                modelObject = JsonParser.parseString(new String(this.fileMap.get(modelPath), StandardCharsets.UTF_8)).getAsJsonObject();
            }

            if (modelObject != null && modelObject.has("overrides")) {
                JsonArray array = modelObject.getAsJsonArray("overrides");

                for (JsonElement element : array) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    jsonObject.get("predicate").getAsJsonObject().addProperty("custom_model_data", cmdInfo.value());
                    jsonArray.add(jsonObject);
                }
            }

            return true;
        } catch (Exception e) {
            CommonImpl.LOGGER.error(String.format("Something went wrong while adding custom model data (%s) of %s for model %s", cmdInfo.value(), Registries.ITEM.getId(cmdInfo.item()), cmdInfo.modelPath().toString()));
            e.printStackTrace();
            return false;
        }
    }

    public JsonArray getCustomModels(Item item, OverridePlace place) {
        if (!this.customModels.containsKey(item)) {
            var places = new JsonArray[OverridePlace.values().length];
            this.customModels.put(item, places);
        }

        var json = this.customModels.get(item)[place.ordinal()];

        if (json == null) {
            json = new JsonArray();
            this.customModels.get(item)[place.ordinal()] = json;
        }
        return json;
    }

    @Override
    public boolean addArmorModel(PolymerArmorModel armorModel) {
        return this.armors.add(armorModel);
    }

    @Override
    public byte[] getData(String path) {
        return this.fileMap.get(path);
    }

    @Override
    @Nullable
    public byte[] getDataOrSource(String path) {
        if (this.fileMap.containsKey(path)) {
            return this.fileMap.get(path);
        } else {
            return this.getSourceData(path);
        }
    }

    @Override
    public boolean addAssetsSource(String modId) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            this.rootPaths.addAll(FabricLoader.getInstance().getModContainer(modId).get().getRootPaths());
            return true;
        }

        return false;
    }

    @Override
    public void addWriteConverter(BiFunction<String, byte[], @Nullable byte[]> converter) {
        this.converters.add(converter);
    }

    @Nullable
    private byte[] getSourceData(String path) {
        try {
            var stream = getSourceStream(path);
            if (stream != null) {
                return stream.readAllBytes();
            }
        } catch (Throwable e) {
            CommonImpl.LOGGER.warn("Error occurred while getting data from vanilla jar!", e);
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
            CommonImpl.LOGGER.warn("Error occurred while getting data from vanilla jar!", e);
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

                for (var entry : this.modsList) {
                    var b = new StringBuilder(" - ").append( entry.getMetadata().getName()).append(" (").append(entry.getMetadata().getId()).append(")");
                    if (!entry.getMetadata().getLicense().isEmpty()) {
                        b.append(" / License: ");
                        var iter = entry.getMetadata().getLicense().iterator();
                        while(iter.hasNext()) {
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

                boolean bool = true;
                {
                    var jsonObject = new JsonObject();
                    var sorted = new ArrayList<>(this.customModelData.entrySet());
                    sorted.sort(Map.Entry.comparingByKey());
                    for (var entry : sorted) {
                        var jsonObject2 = new JsonObject();
                        for (var model : entry.getValue()) {
                            jsonObject2.addProperty(CommonImplUtils.shortId(model.modelPath()), model.value());
                        }

                        jsonObject.add(CommonImplUtils.shortId(entry.getKey()), jsonObject2);
                    }

                    this.fileMap.put("assets/polymer/items.json", GSON.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
                }


                for (var key : this.customModels.keySet()) {
                    Identifier id = Registries.ITEM.getId(key);
                    try {
                        JsonObject modelObject;

                        String baseModelPath;
                        {
                            Identifier itemId = Registries.ITEM.getId(key);
                            baseModelPath = "assets/" + itemId.getNamespace() + "/models/item/" + itemId.getPath() + ".json";
                        }

                        modelObject = JsonParser.parseString(new String(this.getDataOrSource(baseModelPath), StandardCharsets.UTF_8)).getAsJsonObject();


                        if (modelObject.has("overrides")) {
                            this.getCustomModels(key, OverridePlace.EXISTING).addAll(modelObject.getAsJsonArray("overrides"));
                        }

                        var jsonArray = new JsonArray();

                        for (var models : this.customModels.get(key)) {
                            if (models != null) {
                                jsonArray.addAll(models);
                            }
                        }

                        modelObject.add("overrides", jsonArray);

                        this.fileMap.put(baseModelPath, modelObject.toString().getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        CommonImpl.LOGGER.error("Something went wrong while saving model of " + id);
                        e.printStackTrace();
                        bool = false;
                    }
                }

                for (var entry : this.atlasDefinitions.entrySet()) {
                    var obj = new JsonObject();
                    obj.add("sources", entry.getValue());
                    this.fileMap.put(entry.getKey(), obj.toString().getBytes(StandardCharsets.UTF_8));
                }

                if (!this.armors.isEmpty()) {
                    credits.add("Armor texture support is based on https://github.com/Ancientkingg/fancyPants");
                    credits.add("");

                    var list = new ArrayList<ArmorData>();

                    int globalScale = 1;

                    var armorDataMap = new HashMap<Integer, String>();

                    for (var entry : this.armors) {
                        armorDataMap.put(entry.color(), CommonImplUtils.shortId(entry.modelPath()));
                        try {
                            var images = new BufferedImage[2];
                            var metadata = new ArmorTextureMetadata[2];

                            for (int i = 0; i <= 1; i++) {
                                BufferedImage bi = null;

                                {
                                    var path = "assets/" + entry.modelPath().getNamespace() + "/textures/models/armor/" + entry.modelPath().getPath() + "_layer_" + (i + 1) + ".png";
                                    var data = this.fileMap.get(path);

                                    if (data == null) {
                                        try {
                                            InputStream stream = this.getSourceStream(path);
                                            if (stream != null) {
                                                bi = ImageIO.read(stream);
                                            }
                                        } catch (Exception e) {
                                            // silence!
                                        }
                                    } else {
                                        bi = ImageIO.read(new ByteArrayInputStream(data));
                                    }

                                    images[i] = bi;
                                }
                                {
                                    var path = AssetPaths.armorTexturePolymerMetadata(entry.modelPath(), i + 1);
                                    var data = this.fileMap.get(path);

                                    if (data != null) {
                                        int finalI = i;
                                        ArmorTextureMetadata.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(new String(data))).result()
                                                .ifPresentOrElse((r) -> metadata[finalI] = r.getFirst(), () -> metadata[finalI] = ArmorTextureMetadata.DEFAULT);
                                    } else {
                                        metadata[i] = ArmorTextureMetadata.DEFAULT;
                                    }
                                    var scaleTest1 = (double) globalScale / metadata[i].scale();
                                    if (scaleTest1 != Math.floor(scaleTest1)) {
                                        var scaleTest2 = (double) metadata[i].scale() / globalScale;
                                        if (scaleTest2 != Math.floor(scaleTest2)) {
                                            globalScale *= metadata[i].scale();
                                        } else {
                                            globalScale = metadata[i].scale();
                                        }
                                    }
                                }
                            }
                            list.add(new ArmorData(entry.modelPath(), entry.color(), images, metadata));
                        } catch (Throwable e) {
                            CommonImpl.LOGGER.error("Error occurred when creating " + entry.modelPath() + " armor texture!");
                            e.printStackTrace();
                        }
                    }
                    list.sort(Comparator.comparing(e -> -e.color()));

                    this.fileMap.put("assets/polymer/armors.json", GSON.toJson(armorDataMap).getBytes(StandardCharsets.UTF_8));
                    this.fileMap.put(armorTexture(vId("vanilla_leather"), 1), this.getSourceData(armorTexture(new Identifier("leather"), 1)));
                    this.fileMap.put(armorOverlayTexture(vId("vanilla_leather"), 1), this.getSourceData(armorOverlayTexture(vId("leather"), 1)));
                    this.fileMap.put(armorTexture(vId("vanilla_leather"), 2), this.getSourceData(armorTexture(new Identifier("leather"), 2)));
                    this.fileMap.put(armorOverlayTexture(vId("vanilla_leather"), 2), this.getSourceData(armorOverlayTexture(vId("leather"), 2)));
                    int[] width = new int[]{64 * globalScale, 64 * globalScale};
                    int[] height = new int[]{32 * globalScale, 32 * globalScale};

                    for (var entry : new ArrayList<>(list)) {
                        for (int i = 0; i <= 1; i++) {
                            var image = entry.images()[i];
                            var metadata = entry.metadata()[i];

                            if (image != null) {
                                var scale = globalScale / metadata.scale();
                                var newHeight = image.getHeight() * scale;
                                var newWidth = image.getWidth() * scale;

                                var check = (double) newWidth / (64 * globalScale);
                                if (check != Math.floor(check)) {
                                    CommonImpl.LOGGER.warn("Invalid texture size for armor " + entry.identifier() + " (" + i + ")! Skipping...");
                                    list.remove(entry);
                                    continue;
                                }

                                height[i] = Math.max(height[i], newHeight);
                                width[i] += newWidth;
                            }

                        }
                    }

                    var image = new BufferedImage[]{new BufferedImage(width[0], height[0], BufferedImage.TYPE_INT_ARGB), new BufferedImage(width[1], height[1], BufferedImage.TYPE_INT_ARGB)};

                    int[] cWidth = new int[]{64 * globalScale, 64 * globalScale};

                    var graphics = new Graphics[]{image[0].getGraphics(), image[1].getGraphics()};

                    try {
                        for (int i = 0; i <= 1; i++) {
                            {
                                //noinspection ConstantConditions
                                var tex = ImageIO.read(this.getSourceStream(armorTexture(vId("leather"), i + 1)));
                                graphics[i].drawImage(tex, 0, 0, tex.getWidth() * globalScale, tex.getHeight() * globalScale, null);
                            }
                            {
                                //noinspection ConstantConditions
                                var tex = ImageIO.read(this.getSourceStream(armorOverlayTexture(vId("leather"), i + 1)));
                                graphics[i].drawImage(tex, 0, 0, tex.getWidth() * globalScale, tex.getHeight() * globalScale, null);
                            }
                            graphics[i].setColor(Color.WHITE);
                            graphics[i].drawRect(0, 1, 0, 0);
                        }


                        for (var entry : list) {
                            for (int i = 0; i <= 1; i++) {
                                var metadata = entry.metadata()[i];
                                var scale = globalScale / metadata.scale();
                                var tmpImage = entry.images()[i];

                                if (tmpImage == null) {
                                    continue;
                                }
                                graphics[i].drawImage(tmpImage, cWidth[i], 0, tmpImage.getWidth() * scale, tmpImage.getHeight() * scale, null);

                                graphics[i].setColor(new Color(entry.color() | 0xFF000000));
                                graphics[i].drawRect(cWidth[i], 0, 0, 0);

                                if ((metadata.frames() != 0 && metadata.animationSpeed() != 0) || metadata.interpolate()) {
                                    graphics[i].setColor(new Color(metadata.frames(), metadata.animationSpeed(), metadata.interpolate() ? 1 : 0));
                                    graphics[i].drawRect(cWidth[i] + 1, 0, 0, 0);
                                }

                                if (metadata.emissivity() != 0) {
                                    graphics[i].setColor(new Color(metadata.emissivity(), 0, 0));
                                    graphics[i].drawRect(cWidth[i] + 2, 0, 0, 0);
                                }

                                cWidth[i] += tmpImage.getWidth() * scale;
                            }
                        }

                        for (int i = 0; i <= 1; i++) {
                            graphics[i].dispose();

                            {
                                var out = new ByteArrayOutputStream();
                                ImageIO.write(image[i], "png", out);
                                this.fileMap.put(armorTexture(vId("leather"), i + 1), out.toByteArray());
                            }
                            {
                                var out = new ByteArrayOutputStream();
                                ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", out);
                                this.fileMap.put(armorOverlayTexture(vId("leather"), i + 1), out.toByteArray());
                            }
                        }
                    } catch (Throwable e) {
                        CommonImpl.LOGGER.error("Error occurred when creating armor texture!");
                        e.printStackTrace();
                    }

                    for (String string : new String[]{"fsh", "json", "vsh"}) {
                        this.fileMap.put(
                                "assets/minecraft/shaders/core/rendertype_armor_cutout_no_cull." + string,
                                Files.readString(getSelfPath("base-armor/rendertype_armor_cutout_no_cull." + string))
                                        .replace("${polymer_texture_resolution}", "" + (16 * globalScale))
                                        .getBytes(StandardCharsets.UTF_8)
                        );
                    }

                }

                if (!this.fileMap.containsKey(AssetPaths.PACK_METADATA)) {
                    this.fileMap.put(AssetPaths.PACK_METADATA, ("" +
                            "{\n" +
                            "   \"pack\":{\n" +
                            "      \"pack_format\":" + SharedConstants.RESOURCE_PACK_VERSION + ",\n" +
                            "      \"description\":\"Server resource pack\"\n" +
                            "   }\n" +
                            "}\n").getBytes(StandardCharsets.UTF_8));
                }


                if (!this.fileMap.containsKey(AssetPaths.PACK_ICON)) {
                    var filePath = FabricLoader.getInstance().getGameDir().resolve("server-icon.png");

                    if (filePath.toFile().exists()) {
                        this.fileMap.put(AssetPaths.PACK_ICON, Files.readAllBytes(filePath));
                    } else {
                        this.fileMap.put(AssetPaths.PACK_ICON, Files.readAllBytes(getSelfPath("assets/icon.png")));
                    }
                }

                this.fileMap.put("polymer-credits.txt", String.join("\n", credits).getBytes(StandardCharsets.UTF_8));

                {
                    var outputStream = new ZipOutputStream(new FileOutputStream(this.outputPath.toFile()));

                    {
                        for (var path : this.fileMap.keySet().toArray(new String[0])) {
                            var split = new ArrayList<>(List.of(path.split("/")));
                            while (split.size() > 1) {
                                split.remove(split.size() - 1);

                                this.fileMap.put(String.join("/", split) + "/", null);
                            }

                        }
                    }

                    var sorted = new ArrayList<>(this.fileMap.entrySet());
                    sorted.sort(Map.Entry.comparingByKey());
                    for (var entry : sorted) {
                        var outByte = entry.getValue();
                        var path = entry.getKey();
                        if (outByte != null) {
                            for (var conv : converters) {
                                outByte = conv.apply(path, outByte);
                                if (outByte == null) {
                                    break;
                                }
                            }

                            if (outByte == null) {
                                continue;
                            }
                        }

                        var zipEntry = new ZipEntry(path);
                        zipEntry.setTime(0);
                        outputStream.putNextEntry(zipEntry);
                        if (outByte != null) {
                            outputStream.write(outByte);
                        }
                        outputStream.closeEntry();
                    }

                    outputStream.close();
                }

                return bool;
            } catch (Exception e) {
                CommonImpl.LOGGER.error("Something went wrong while creating resource pack!", e);
                return false;
            }
        });
    }

    private Identifier vId(String path) {
        return new Identifier(path);
    }

    public enum OverridePlace {
        BEFORE_EXISTING,
        EXISTING,
        BEFORE_CUSTOM_MODEL_DATA,
        CUSTOM_MODEL_DATA,
        END
    }

    private record ArmorData(Identifier identifier, int color, BufferedImage[] images,
                             ArmorTextureMetadata[] metadata) {
    }
}
