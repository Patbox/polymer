package eu.pb4.polymer.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.polymer.PolymerMod;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipFile;

class DefaultRPBuilder implements RPBuilder {
    private final static String CLIENT_URL = "https://launcher.mojang.com/v1/objects/1cf89c77ed5e72401b869f66410934804f3d6f52/client.jar";

    private final Path mainPath;
    private final Path outputPath;
    private final Path inputPath;
    private final ZipFile clientJar;
    private final Map<Item, JsonArray> models = new HashMap<>();

    DefaultRPBuilder(Path mainPath) throws Exception {
        this.mainPath = mainPath;
        this.outputPath = this.mainPath.resolve("output");
        this.inputPath = this.mainPath.resolve("input");

        mainPath.toFile().mkdirs();
        FileUtils.deleteDirectory(this.outputPath.toFile());

        this.outputPath.toFile().mkdirs();
        this.inputPath.toFile().mkdirs();

        FileUtils.copyDirectory(this.inputPath.toFile(), this.outputPath.toFile());

        Path clientJarPath = this.mainPath.resolve("client.jar");

        if (!clientJarPath.toFile().exists()) {
            PolymerMod.LOGGER.info("Downloading vanilla client jar...");
            URL url = new URL(CLIENT_URL);
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            Files.copy(is, clientJarPath);
        }

        this.clientJar = new ZipFile(clientJarPath.toFile());
    }


    @Override
    public boolean addData(String path, byte[] data) {
        Path realPath = this.outputPath.resolve(path);
        realPath.toFile().mkdirs();
        try {
            Files.write(realPath, data, StandardOpenOption.CREATE);
            return true;
        } catch (Exception e) {
            PolymerMod.LOGGER.error("Something went wrong while adding raw data to path: " + path);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean copyModAssets(String modId) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        if (mod.isPresent()) {
            ModContainer container = mod.get();
            try {
                Path assets = container.getPath("assets");
                Path output = this.outputPath.resolve("assets");
                Files.walkFileTree(assets, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path fileOut;
                        try {
                            fileOut = output.resolve(assets.relativize(file).toString());
                        } catch (Exception e) {
                            fileOut = output.resolve(file.toString());

                        }

                        try {
                            fileOut.getParent().toFile().mkdirs();
                        } catch (Exception e) {
                        }

                        Files.copy(file, fileOut);
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
    public boolean addCustomModelData(CMDInfo cmdInfo) {
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
            {
                String baseModelPath = "assets/" + cmdInfo.modelPath().getNamespace() + "/models/" + cmdInfo.modelPath().getPath() + ".json";
                Path inputPath = this.outputPath.resolve(baseModelPath);

                if (inputPath.toFile().exists()) {
                    JsonObject modelObject = ResourcePackUtils.JSON_PARSER.parse(Files.readString(inputPath)).getAsJsonObject();
                    if (modelObject.has("overrides")) {
                        JsonArray array = modelObject.getAsJsonArray("overrides");

                        for (JsonElement element : array) {
                            JsonObject jsonObject = element.getAsJsonObject();
                            jsonObject.get("predicate").getAsJsonObject().addProperty("custom_model_data", cmdInfo.value());
                            jsonArray.add(jsonObject);
                        }
                    }
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
    public boolean finish() {
        boolean bool = true;
        for (Map.Entry<Item, JsonArray> entry : this.models.entrySet()) {
            Identifier id = Registry.ITEM.getId(entry.getKey());
            try {
                Path basePath = this.outputPath.resolve("assets/" + id.getNamespace() + "/models/item/");
                basePath.toFile().mkdirs();
                JsonObject modelObject;

                String baseModelPath;
                {
                    Identifier itemId = Registry.ITEM.getId(entry.getKey());
                    baseModelPath = "assets/" + itemId.getNamespace() + "/models/item/" + itemId.getPath() + ".json";
                }

                Path inputPath = this.inputPath.resolve(baseModelPath);

                if (inputPath.toFile().exists()) {
                    modelObject = ResourcePackUtils.JSON_PARSER.parse(Files.readString(inputPath)).getAsJsonObject();
                } else {
                    InputStream stream = this.clientJar.getInputStream(this.clientJar.getEntry(baseModelPath));
                    modelObject = ResourcePackUtils.JSON_PARSER.parse(IOUtils.toString(stream, StandardCharsets.UTF_8.name())).getAsJsonObject();
                }

                JsonArray jsonArray = new JsonArray();

                if (modelObject.has("overrides")) {
                    jsonArray.addAll(modelObject.getAsJsonArray("overrides"));
                }
                jsonArray.addAll(entry.getValue());

                modelObject.add("overrides", jsonArray);

                Files.writeString(basePath.resolve(id.getPath() + ".json"), modelObject.toString());
            } catch (Exception e) {
                PolymerMod.LOGGER.error("Something went wrong while saving model of " + id);
                e.printStackTrace();
                bool = false;
            }
        }

        try {
            {
                Path packMCData = this.outputPath.resolve("pack.mcmeta");
                if (!packMCData.toFile().exists()) {
                    Files.writeString(packMCData, "" +
                            "{\n" +
                            "   \"pack\":{\n" +
                            "      \"pack_format\":" + SharedConstants.RESOURCE_PACK_VERSION + ",\n" +
                            "      \"description\":\"Server resource pack\"\n" +
                            "   }\n" +
                            "}\n");
                }
            }
            {
                Path packMCData = this.outputPath.resolve("pack.png");
                if (!packMCData.toFile().exists()) {
                    Files.copy(FabricLoader.getInstance().getModContainer("polymer").get()
                                    .getPath("assets/icon.png"),
                            packMCData);
                }
            }
        } catch (Exception e) {
            PolymerMod.LOGGER.error("Something went wrong while creating pack.mcmeta file.");
            e.printStackTrace();
            bool = false;
        }

        return bool;
    }
}
