package eu.pb4.polymer.impl.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import eu.pb4.polymer.impl.PolymerMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@ApiStatus.Internal
public class DefaultRPBuilder implements InternalRPBuilder {
    static final JsonParser JSON_PARSER = new JsonParser();

    private final static String CLIENT_URL = "https://launcher.mojang.com/v1/objects/1cf89c77ed5e72401b869f66410934804f3d6f52/client.jar";

    private final Path inputPath;
    private final Map<Item, JsonArray> models = new HashMap<>();
    private final ZipOutputStream outputStream;
    private ZipFile clientJar = null;

    public DefaultRPBuilder(Path outputPath, @Nullable Path inputPath) throws Exception {
        outputPath.getParent().toFile().mkdirs();

        if (outputPath.toFile().exists()) {
            Files.deleteIfExists(outputPath);
        }

        this.outputStream = new ZipOutputStream(new FileOutputStream(outputPath.toFile()));

        this.inputPath = inputPath;

        if (inputPath != null && inputPath.toFile().exists() && inputPath.toFile().isDirectory()) {

        }
    }


    @Override
    public boolean addData(String path, byte[] data) {
        try {
            this.outputStream.putNextEntry(new ZipEntry(path));
            this.outputStream.write(data, 0, data.length);
            this.outputStream.closeEntry();
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
                Files.walkFileTree(assets, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        var relative = assets.relativize(file);

                        outputStream.putNextEntry(new ZipEntry("assets/" + relative));
                        var bytes = Files.readAllBytes(file);

                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();

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
            if (this.inputPath != null) {
                String baseModelPath = "assets/" + cmdInfo.modelPath().getNamespace() + "/models/" + cmdInfo.modelPath().getPath() + ".json";
                Path inputPath = this.inputPath.resolve(baseModelPath);

                if (inputPath.toFile().exists()) {
                    JsonObject modelObject = JSON_PARSER.parse(Files.readString(inputPath)).getAsJsonObject();
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
    public CompletableFuture<Boolean> buildResourcePack() {

        return CompletableFuture.supplyAsync(() -> {
            try {
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

                        Path inputPath = this.inputPath != null ? this.inputPath.resolve(baseModelPath) : null;

                        if (inputPath != null && inputPath.toFile().exists()) {
                            modelObject = JSON_PARSER.parse(Files.readString(inputPath)).getAsJsonObject();
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

                        this.outputStream.putNextEntry(new ZipEntry(basePath + id.getPath() + ".json"));
                        var bytes = modelObject.toString().getBytes(StandardCharsets.UTF_8);

                        this.outputStream.write(bytes, 0, bytes.length);
                        this.outputStream.closeEntry();

                    } catch (Exception e) {
                        PolymerMod.LOGGER.error("Something went wrong while saving model of " + id);
                        e.printStackTrace();
                        bool = false;
                    }
                }

                try {
                    {
                        if (this.inputPath == null || !this.inputPath.resolve("pack.mcmeta").toFile().exists()) {
                            this.outputStream.putNextEntry(new ZipEntry("pack.mcmeta"));
                            var bytes = ("" +
                                    "{\n" +
                                    "   \"pack\":{\n" +
                                    "      \"pack_format\":" + SharedConstants.field_29738 + ",\n" +
                                    "      \"description\":\"Server resource pack\"\n" +
                                    "   }\n" +
                                    "}\n").getBytes(StandardCharsets.UTF_8);
                            this.outputStream.write(bytes, 0, bytes.length);
                            this.outputStream.closeEntry();
                        }
                    }
                    {
                        if (this.inputPath == null || !this.inputPath.resolve("pack.png").toFile().exists()) {
                            this.outputStream.putNextEntry(new ZipEntry("pack.png"));

                            var bytes = Files.readAllBytes(FabricLoader.getInstance().getModContainer("polymer").get().getPath("assets/icon.png"));
                            this.outputStream.write(bytes, 0, bytes.length);
                            this.outputStream.closeEntry();
                        }
                    }
                } catch (Exception e) {
                    PolymerMod.LOGGER.error("Something went wrong while creating pack.mcmeta file.");
                    e.printStackTrace();
                    bool = false;
                }

                this.outputStream.close();
                return bool;
            } catch (Exception e) {
                PolymerMod.LOGGER.error("Something went wrong while creating resource pack!");
                e.printStackTrace();
                return false;
            }

        });
    }
}
