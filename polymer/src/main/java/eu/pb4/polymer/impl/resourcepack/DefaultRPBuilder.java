package eu.pb4.polymer.impl.resourcepack;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.api.resourcepack.PolymerArmorModel;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.PolymerImpl;
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
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class DefaultRPBuilder implements InternalRPBuilder {
    public static final Gson GSON = PolymerImpl.GSON;
    public final SimpleEvent<Consumer<List<String>>> buildEvent = new SimpleEvent<>();
    private final Map<Item, JsonArray> models = new HashMap<>();
    private final TreeMap<String, byte[]> fileMap = new TreeMap<>();
    private final List<PolymerArmorModel> armors = new ArrayList<>();
    private final Path outputPath;
    private final List<ModContainer> modsList = new ArrayList<>();
    private final Map<Identifier, List<PolymerModelData>> customModelData = new HashMap<>();
    private ZipFile clientJar = null;


    public DefaultRPBuilder(Path outputPath) {
        outputPath.getParent().toFile().mkdirs();
        this.outputPath = outputPath;

        try {
            if (outputPath.toFile().exists()) {
                Files.deleteIfExists(outputPath);
            }
        } catch (Exception e) {
            PolymerImpl.LOGGER.warn("Couldn't remove " + outputPath + " file!");
            e.printStackTrace();
        }

    }

    private static Path getPolymerPath(String path) {
        return FabricLoader.getInstance().getModContainer("polymer").get().getPath(path);
    }

    @Override
    public boolean addData(String path, byte[] data) {
        try {
            this.fileMap.put(path, data);
            return true;
        } catch (Exception e) {
            PolymerImpl.LOGGER.error("Something went wrong while adding raw data to path: " + path);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean copyFromPath(Path basePath, boolean override) {
        try {
            if (Files.isSymbolicLink(basePath)) {
                basePath = Files.readSymbolicLink(basePath);
            }

            if (Files.isDirectory(basePath)) {
                Path finalBasePath = basePath;
                Files.walk(basePath).forEach((file) -> {
                    var relative = finalBasePath.relativize(file);
                    var path = relative.toString().replace("\\", "/");
                    if ((override || !fileMap.containsKey(path)) && Files.isRegularFile(file)) {
                        try {
                            fileMap.put(path, Files.readAllBytes(file));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

                return true;
            } else if (Files.isRegularFile(basePath)) {
                try (var fs = FileSystems.newFileSystem(basePath, Collections.emptyMap())) {
                    fs.getRootDirectories().forEach(this::copyFromPath);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            PolymerImpl.LOGGER.error("Something went wrong while copying data from: " + basePath);
            e.printStackTrace();
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
                    Path assets = rootPaths.resolve("assets");
                    Files.walk(assets).forEach((file) -> {
                        var relative = assets.relativize(file);
                        var path = relative.toString().replace("\\", "/");
                        if (Files.isRegularFile(file)) {
                            try {
                                fileMap.put(path, Files.readAllBytes(file));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                return true;
            } catch (Exception e) {
                PolymerImpl.LOGGER.error("Something went wrong while copying assets of mod: " + modId);
                e.printStackTrace();
                return false;
            }
        }
        PolymerImpl.LOGGER.warn("Tried to copy assets from non existing mod " + modId);
        return false;
    }

    @Override
    public boolean addCustomModelData(PolymerModelData cmdInfo) {
        try {
            JsonArray jsonArray;

            if (this.models.containsKey(cmdInfo.item())) {
                jsonArray = this.models.get(cmdInfo.item());
            } else {
                jsonArray = new JsonArray();
                this.models.put(cmdInfo.item(), jsonArray);
            }

            this.customModelData.computeIfAbsent(Registries.ITEM.getId(cmdInfo.item()), (x) -> new ArrayList<>()).add(cmdInfo);

            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("model", cmdInfo.modelPath().toString());
                JsonObject predicateObject = new JsonObject();
                predicateObject.addProperty("custom_model_data", cmdInfo.value());
                jsonObject.add("predicate", predicateObject);

                jsonArray.add(jsonObject);
            }
            JsonObject modelObject = null;
            var modelPath = "assets/" + cmdInfo.modelPath().getNamespace() + "/models/" + cmdInfo.modelPath().getPath() + ".json";

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
            PolymerImpl.LOGGER.error(String.format("Something went wrong while adding custom model data (%s) of %s for model %s", cmdInfo.value(), Registries.ITEM.getId(cmdInfo.item()), cmdInfo.modelPath().toString()));
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addArmorModel(PolymerArmorModel armorModel) {
        return this.armors.add(armorModel);
    }

    @Override
    public byte[] getData(String path) {
        return this.fileMap.get(path);
    }

    @Nullable
    public byte[] getDataOrVanilla(String path) {
        if (this.fileMap.containsKey(path)) {
            return this.fileMap.get(path);
        } else {
            try {
                var entry = this.clientJar.getEntry(path);

                if (entry != null) {
                    InputStream stream = this.clientJar.getInputStream(entry);

                    return stream.readAllBytes();
                }
            } catch (Exception e) {
                PolymerImpl.LOGGER.warn("Error occurred while getting data from vanilla jar! {}", e);
            }
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
                credits.add("Generated with Polymer " + PolymerImpl.VERSION);
                credits.add("");
                credits.add("Vanilla assets by Mojang Studios");
                credits.add("");
                credits.add("Used mod assets: ");

                for (var entry : this.modsList) {
                    credits.add(" - " + entry.getMetadata().getName() + " (" + entry.getMetadata().getId() + ")");
                }
                credits.add("");

                this.clientJar = PolymerUtils.getClientJar();

                this.buildEvent.invoke((c) -> c.accept(credits));

                boolean bool = true;
                {
                    var jsonObject = new JsonObject();
                    for (var entry : this.customModelData.entrySet()) {
                        var jsonObject2 = new JsonObject();
                        for (var model : entry.getValue()) {
                            jsonObject2.addProperty(model.modelPath().toString(), model.value());
                        }

                        jsonObject.add(entry.getKey().toString(), jsonObject2);
                    }

                    this.fileMap.put("assets/polymer/items.json", GSON.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
                }


                for (Map.Entry<Item, JsonArray> entry : this.models.entrySet()) {
                    Identifier id = Registries.ITEM.getId(entry.getKey());
                    try {
                        JsonObject modelObject;

                        String baseModelPath;
                        {
                            Identifier itemId = Registries.ITEM.getId(entry.getKey());
                            baseModelPath = "assets/" + itemId.getNamespace() + "/models/item/" + itemId.getPath() + ".json";
                        }

                        modelObject = JsonParser.parseString(new String(this.getDataOrVanilla(baseModelPath), StandardCharsets.UTF_8)).getAsJsonObject();

                        JsonArray jsonArray = new JsonArray();

                        if (modelObject.has("overrides")) {
                            jsonArray.addAll(modelObject.getAsJsonArray("overrides"));
                        }
                        jsonArray.addAll(entry.getValue());

                        modelObject.add("overrides", jsonArray);

                        this.fileMap.put(baseModelPath, modelObject.toString().getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        PolymerImpl.LOGGER.error("Something went wrong while saving model of " + id);
                        e.printStackTrace();
                        bool = false;
                    }
                }
                if (this.armors.size() > 0) {
                    credits.add("Armor texture support is based on https://github.com/Ancientkingg/fancyPants");
                    credits.add("");

                    var list = new ArrayList<ArmorData>();

                    int globalScale = 1;

                    var armorDataMap = new HashMap<Integer, String>();

                    for (var entry : this.armors) {
                        armorDataMap.put(entry.value(), entry.modelPath().toString());
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
                                            InputStream stream = this.clientJar.getInputStream(this.clientJar.getEntry(path));
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
                                    var path = "assets/" + entry.modelPath().getNamespace() + "/textures/models/armor/" + entry.modelPath().getPath() + "_layer_" + (i + 1) + ".polymer.json";
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
                            list.add(new ArmorData(entry.modelPath(), entry.value(), images, metadata));
                        } catch (Throwable e) {
                            PolymerImpl.LOGGER.error("Error occurred when creating " + entry.modelPath() + " armor texture!");
                            e.printStackTrace();
                        }
                    }
                    list.sort(Comparator.comparing(e -> -e.color()));

                    this.fileMap.put("assets/polymer/armors.json", GSON.toJson(armorDataMap).getBytes(StandardCharsets.UTF_8));
                    this.fileMap.put("assets/minecraft/textures/models/armor/vanilla_leather_layer_1.png", this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_1.png")).readAllBytes());
                    this.fileMap.put("assets/minecraft/textures/models/armor/vanilla_leather_layer_1_overlay.png", this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_1_overlay.png")).readAllBytes());
                    this.fileMap.put("assets/minecraft/textures/models/armor/vanilla_leather_layer_2.png", this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_2.png")).readAllBytes());
                    this.fileMap.put("assets/minecraft/textures/models/armor/vanilla_leather_layer_2_overlay.png", this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_2_overlay.png")).readAllBytes());

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
                                    PolymerImpl.LOGGER.warn("Invalid texture size for armor " + entry.identifier() + " (" + i + ")! Skipping...");
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
                                var tex = ImageIO.read(this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_" + (i + 1) + ".png")));
                                graphics[i].drawImage(tex, 0, 0, tex.getWidth() * globalScale, tex.getHeight() * globalScale, null);
                            }
                            {
                                var tex = ImageIO.read(this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_" + (i + 1) + "_overlay.png")));
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
                                this.fileMap.put("assets/minecraft/textures/models/armor/leather_layer_" + (i + 1) + ".png", out.toByteArray());
                            }
                            {
                                var out = new ByteArrayOutputStream();
                                ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", out);
                                this.fileMap.put("assets/minecraft/textures/models/armor/leather_layer_" + (i + 1) + "_overlay.png", out.toByteArray());
                            }
                        }
                    } catch (Throwable e) {
                        PolymerImpl.LOGGER.error("Error occurred when creating armor texture!");
                        e.printStackTrace();
                    }

                    for (String string : new String[]{"fsh", "json", "vsh"}) {
                        this.fileMap.put(
                                "assets/minecraft/shaders/core/rendertype_armor_cutout_no_cull." + string,
                                Files.readString(getPolymerPath("base-armor/rendertype_armor_cutout_no_cull." + string))
                                        .replace("${polymer_texture_resolution}", "" + (16 * globalScale))
                                        .getBytes(StandardCharsets.UTF_8)
                        );
                    }

                }

                if (!this.fileMap.containsKey("pack.mcmeta")) {
                    this.fileMap.put("pack.mcmeta", ("" +
                            "{\n" +
                            "   \"pack\":{\n" +
                            "      \"pack_format\":" + SharedConstants.RESOURCE_PACK_VERSION + ",\n" +
                            "      \"description\":\"Server resource pack\"\n" +
                            "   }\n" +
                            "}\n").getBytes(StandardCharsets.UTF_8));
                }


                if (!this.fileMap.containsKey("pack.png")) {
                    var filePath = FabricLoader.getInstance().getGameDir().resolve("server-icon.png");

                    if (filePath.toFile().exists()) {
                        this.fileMap.put("pack.png", Files.readAllBytes(filePath));
                    } else {
                        this.fileMap.put("pack.png", Files.readAllBytes(getPolymerPath("assets/icon.png")));
                    }
                }

                this.fileMap.put("polymer-about.txt", String.join("\n", credits).getBytes(StandardCharsets.UTF_8));

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
                    sorted.sort(Comparator.comparing(e -> e.getKey()));
                    for (var entry : sorted) {
                        var zipEntry = new ZipEntry(entry.getKey());
                        zipEntry.setTime(0);
                        outputStream.putNextEntry(zipEntry);
                        var value = entry.getValue();
                        if (value != null) {
                            outputStream.write(entry.getValue());
                        }
                        outputStream.closeEntry();
                    }

                    outputStream.close();
                }

                return bool;
            } catch (Exception e) {
                PolymerImpl.LOGGER.error("Something went wrong while creating resource pack!");
                e.printStackTrace();
                return false;
            }
        });
    }


    private record ArmorData(Identifier identifier, int color, BufferedImage[] images,
                             ArmorTextureMetadata[] metadata) {
    }
}
