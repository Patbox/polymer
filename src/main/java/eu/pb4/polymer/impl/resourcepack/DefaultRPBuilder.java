package eu.pb4.polymer.impl.resourcepack;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.api.resourcepack.PolymerArmorModel;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import eu.pb4.polymer.impl.PolymerMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.ApiStatus;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class DefaultRPBuilder implements InternalRPBuilder {
    public static final JsonParser JSON_PARSER = new JsonParser();
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final static String CLIENT_URL = "https://launcher.mojang.com/v1/objects/1cf89c77ed5e72401b869f66410934804f3d6f52/client.jar";

    private final Map<Item, JsonArray> models = new HashMap<>();
    private final Map<String, byte[]> fileMap = new HashMap<>();
    private final List<PolymerArmorModel> armors = new ArrayList<>();
    private final Path outputPath;
    private ZipFile clientJar = null;
    private final List<ModContainer> modsList = new ArrayList<>();


    public DefaultRPBuilder(Path outputPath) {
        outputPath.getParent().toFile().mkdirs();
        this.outputPath = outputPath;

        try {
            if (outputPath.toFile().exists()) {
                Files.deleteIfExists(outputPath);
            }
        } catch (Exception e) {
            PolymerMod.LOGGER.warn("Couldn't remove " + outputPath + " file!");
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
            PolymerMod.LOGGER.error("Something went wrong while adding raw data to path: " + path);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean copyFromPath(Path basePath) {
        try {
            Files.walkFileTree(basePath, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    var relative = basePath.relativize(file);

                    var bytes = Files.readAllBytes(file);

                    fileMap.put(relative.toString(), bytes);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (Exception e) {
            PolymerMod.LOGGER.error("Something went wrong while copying data from: " + basePath);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean copyModAssets(String modId) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        if (mod.isPresent()) {
            ModContainer container = mod.get();
            this.modsList.add(container);
            try {
                Path assets = container.getPath("assets");
                Files.walkFileTree(assets, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        var relative = assets.relativize(file);

                        var bytes = Files.readAllBytes(file);

                        fileMap.put("assets/" + relative, bytes);

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });

                return true;
            } catch (Exception e) {
                PolymerMod.LOGGER.error("Something went wrong while copying assets of mod: " + modId);
                e.printStackTrace();
                return false;
            }
        }
        PolymerMod.LOGGER.warn("Tried to copy assets from non existing mod " + modId);
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

            if (modelObject == null && this.fileMap.containsKey(modelPath)) {
                modelObject = JSON_PARSER.parse(new String(this.fileMap.get(modelPath), StandardCharsets.UTF_8)).getAsJsonObject();
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
            PolymerMod.LOGGER.error(String.format("Something went wrong while adding custom model data (%s) of %s for model %s", cmdInfo.value(), Registry.ITEM.getId(cmdInfo.item()), cmdInfo.modelPath().toString()));
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addArmorModel(PolymerArmorModel armorModel) {
        return this.armors.add(armorModel);
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
                credits.add("Generated with Polymer " + PolymerMod.VERSION);
                credits.add("");
                credits.add("Vanilla assets by Mojang Studios");
                credits.add("");
                credits.add("Used mod assets: ");

                for (var entry : this.modsList) {
                    credits.add(" - " + entry.getMetadata().getName() + " (" + entry.getMetadata().getId() + ")");
                }
                credits.add("");

                var outputStream = new ZipOutputStream(new FileOutputStream(this.outputPath.toFile()));

                Path clientJarPath;

                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
                    clientJarPath = FabricLoader.getInstance().getGameDir().resolve("assets_client.jar");
                } else {
                    var clientFile = MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                    clientJarPath = Path.of(clientFile);
                }

                if (!clientJarPath.toFile().exists()) {
                    PolymerMod.LOGGER.info("Downloading vanilla client jar...");
                    URL url = new URL(CLIENT_URL);
                    URLConnection connection = url.openConnection();
                    InputStream is = connection.getInputStream();
                    Files.copy(is, clientJarPath);
                }

                this.clientJar = new ZipFile(clientJarPath.toFile());

                boolean bool = true;

                for (Map.Entry<Item, JsonArray> entry : this.models.entrySet()) {
                    Identifier id = Registry.ITEM.getId(entry.getKey());
                    try {
                        String basePath = "assets/" + id.getNamespace() + "/models/item/";
                        JsonObject modelObject;

                        String baseModelPath;
                        {
                            Identifier itemId = Registry.ITEM.getId(entry.getKey());
                            baseModelPath = "assets/" + itemId.getNamespace() + "/models/item/" + itemId.getPath() + ".json";
                        }

                        if (this.fileMap.containsKey(baseModelPath)) {
                            modelObject = JSON_PARSER.parse(new String(this.fileMap.get(baseModelPath), StandardCharsets.UTF_8)).getAsJsonObject();
                        } else {
                            InputStream stream = this.clientJar.getInputStream(this.clientJar.getEntry(baseModelPath));
                            modelObject = JSON_PARSER.parse(IOUtils.toString(stream, StandardCharsets.UTF_8.name())).getAsJsonObject();
                        }

                        JsonArray jsonArray = new JsonArray();

                        if (modelObject.has("overrides")) {
                            jsonArray.addAll(modelObject.getAsJsonArray("overrides"));
                        }
                        jsonArray.addAll(entry.getValue());

                        modelObject.add("overrides", jsonArray);

                        outputStream.putNextEntry(new ZipEntry(basePath + id.getPath() + ".json"));
                        var bytes = modelObject.toString().getBytes(StandardCharsets.UTF_8);

                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();

                    } catch (Exception e) {
                        PolymerMod.LOGGER.error("Something went wrong while saving model of " + id);
                        e.printStackTrace();
                        bool = false;
                    }
                }
                if (this.armors.size() > 0) {
                    credits.add("Armor texture support is based on https://github.com/Ancientkingg/fancyPants");
                    credits.add("");

                    var list = new ArrayList<Triple<Integer, BufferedImage[], ArmorTextureMetadata[]>>();

                    int[] width = new int[]{ 64, 64 };
                    int[] height = new int[]{ 32, 32 };

                    var armorDataMap = new HashMap<Integer, String>();

                    for (var entry : this.armors) {
                        armorDataMap.put(entry.value(), entry.modelPath().toString());
                        try {
                            var a = new BufferedImage[2];
                            var b = new ArmorTextureMetadata[2];
                            BufferedImage bi = null;

                            for (int i = 0; i <= 1; i++) {
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

                                    if (bi != null) {
                                        height[i] = Math.max(height[i], bi.getHeight());
                                        width[i] += bi.getWidth();
                                    }

                                    a[i] = bi;
                                }
                                {
                                    var path = "assets/" + entry.modelPath().getNamespace() + "/textures/models/armor/" + entry.modelPath().getPath() + "_layer_" + (i + 1) + ".polymer.json";
                                    var data = this.fileMap.get(path);

                                    if (data != null) {
                                        int finalI = i;
                                        ArmorTextureMetadata.CODEC.decode(JsonOps.INSTANCE, JSON_PARSER.parse(new String(data))).result()
                                                .ifPresentOrElse((r) -> b[finalI] = r.getFirst(), () -> b[finalI] = ArmorTextureMetadata.DEFAULT);
                                    } else {
                                        b[i] = ArmorTextureMetadata.DEFAULT;
                                    }
                                }
                            }

                            list.add(Triple.of(entry.value(), a, b));
                        } catch (Exception e) {
                            PolymerMod.LOGGER.error("Error occurred when creating " + entry.modelPath() + " armor texture!");
                            e.printStackTrace();
                        }
                    }

                    this.fileMap.put("assets/polymer/armors.json", GSON.toJson(armorDataMap).getBytes(StandardCharsets.UTF_8));
                    this.fileMap.put("assets/minecraft/textures/models/armor/vanilla_leather_layer_1.png", this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_1.png")).readAllBytes());
                    this.fileMap.put("assets/minecraft/textures/models/armor/vanilla_leather_layer_1_overlay.png", this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_1_overlay.png")).readAllBytes());
                    this.fileMap.put("assets/minecraft/textures/models/armor/vanilla_leather_layer_2.png", this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_2.png")).readAllBytes());
                    this.fileMap.put("assets/minecraft/textures/models/armor/vanilla_leather_layer_2_overlay.png", this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_2_overlay.png")).readAllBytes());


                    var image = new BufferedImage[]{new BufferedImage(width[0], height[0], BufferedImage.TYPE_INT_ARGB), new BufferedImage(width[1], height[1], BufferedImage.TYPE_INT_ARGB)};
                    int[] cWidth = new int[] { 64, 64 };

                    var graphics = new Graphics[]{image[0].getGraphics(), image[1].getGraphics()};

                    for (int i = 0; i <= 1; i++) {
                        {
                            var tex = ImageIO.read(this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_" + (i + 1) + ".png")));
                            graphics[i].drawImage(tex, 0, 0, null);
                        }
                        {
                            var tex = ImageIO.read(this.clientJar.getInputStream(this.clientJar.getEntry("assets/minecraft/textures/models/armor/leather_layer_" + (i + 1) + "_overlay.png")));
                            graphics[i].drawImage(tex, 0, 0, null);
                        }
                        graphics[i].setColor(Color.WHITE);
                        graphics[i].drawRect(0, 1, 0, 0);
                    }

                    for (var entry : list) {
                        for (int i = 0; i <= 1; i++) {
                            var metadata = entry.getRight()[i];

                            graphics[i].drawImage(entry.getMiddle()[i], cWidth[i], 0, null);

                            graphics[i].setColor(new Color(entry.getLeft() | 0xFF000000));
                            graphics[i].drawRect(cWidth[i], 0, 0, 0);

                            if ((metadata.frames() != 0 && metadata.animationSpeed() != 0) || metadata.interpolate()) {
                                graphics[i].setColor(new Color(metadata.frames(), metadata.animationSpeed(), metadata.interpolate() ? 1 : 0));
                                graphics[i].drawRect(cWidth[i] + 1, 0, 0, 0);
                            }

                            if (metadata.emissivity() != 0) {
                                graphics[i].setColor(new Color(metadata.emissivity(), 0, 0));
                                graphics[i].drawRect(cWidth[i] + 2, 0, 0, 0);
                            }

                            cWidth[i] += entry.getMiddle()[i].getWidth();
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

                    for (String string : new String[]{"fsh", "json", "vsh"}) {
                        this.fileMap.put(
                                "assets/minecraft/shaders/core/rendertype_armor_cutout_no_cull." + string,
                                Files.readAllBytes(getPolymerPath("base-armor/rendertype_armor_cutout_no_cull." + string))
                        );
                    }

                }

                if (!this.fileMap.containsKey("pack.mcmeta")) {
                    this.fileMap.put("pack.mcmeta", ("" +
                            "{\n" +
                            "   \"pack\":{\n" +
                            "      \"pack_format\":" + SharedConstants.field_29738 + ",\n" +
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

                for (var entry : fileMap.entrySet()) {
                    outputStream.putNextEntry(new ZipEntry(entry.getKey()));
                    outputStream.write(entry.getValue());
                    outputStream.closeEntry();
                }

                outputStream.close();
                return bool;
            } catch (Exception e) {
                PolymerMod.LOGGER.error("Something went wrong while creating resource pack!");
                e.printStackTrace();
                return false;
            }
        });
    }
}
