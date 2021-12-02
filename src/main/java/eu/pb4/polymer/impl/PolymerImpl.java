package eu.pb4.polymer.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientConfig;
import eu.pb4.polymer.impl.compat.CompatStatus;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class PolymerImpl {
    private PolymerImpl() {
    }

    public static final Logger LOGGER = LogManager.getLogger("Polymer");
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final FabricLoader LOADER = FabricLoader.getInstance();

    private static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polymer").get();
    public static final String MOD_ID = CONTAINER.getMetadata().getId();
    public static final String NAME = CONTAINER.getMetadata().getName();
    public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString().split("\\+")[0];

    public static final ServerConfig SERVER_CONFIG = loadConfig("server", ServerConfig.class);

    public static final boolean DEVELOPER_MODE;
    public static final boolean ENABLE_NETWORKING_SERVER;
    public static final boolean ENABLE_NETWORKING_CLIENT;
    public static final boolean USE_ALT_ARMOR_HANDLER;
    public static final boolean FORCE_RESOURCE_PACK_CLIENT;
    public static final boolean FORCE_RESOURCE_PACK_SERVER;
    public static final boolean FORCE_CUSTOM_MODEL_DATA_OFFSET;

    static {

        ENABLE_NETWORKING_SERVER = PolymerImpl.SERVER_CONFIG.enableNetworkSync;
        DEVELOPER_MODE = FabricLoader.getInstance().isDevelopmentEnvironment() || PolymerImpl.SERVER_CONFIG.enableDevUtils;
        FORCE_RESOURCE_PACK_SERVER = PolymerImpl.SERVER_CONFIG.markResourcePackAsForceByDefault;
        FORCE_CUSTOM_MODEL_DATA_OFFSET = PolymerImpl.SERVER_CONFIG.forcePackOffset || CompatStatus.POLYMC;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            var clientConfig = PolymerImpl.loadConfig("client", ClientConfig.class);
            USE_ALT_ARMOR_HANDLER = CompatStatus.REQUIRE_ALT_ARMOR_HANDLER || clientConfig.useAlternativeArmorRenderer;
            ENABLE_NETWORKING_CLIENT = clientConfig.enableNetworkSync;
            FORCE_RESOURCE_PACK_CLIENT = clientConfig.forceResourcePackByDefault;
        } else {
            USE_ALT_ARMOR_HANDLER = false;
            ENABLE_NETWORKING_CLIENT = false;
            FORCE_RESOURCE_PACK_CLIENT = false;
        }
    }

    public static boolean isModLoaded(String modId) {
        return LOADER.isModLoaded(modId);
    }

    public static Path configDir() {
        return LOADER.getConfigDir().resolve("polymer");
    }

    public static <T> T loadConfig(String name, Class<T> clazz) {
        try {
            var folder = configDir();
            if (!folder.toFile().isDirectory()) {
                if (folder.toFile().exists()) {
                    Files.deleteIfExists(folder);
                }
                folder.toFile().mkdirs();
            }
            var path = folder.resolve(name + ".json");

            if (path.toFile().isFile()) {
                String json = IOUtils.toString(new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8));
                var obj = GSON.fromJson(json, clazz);
                saveConfig(name, obj);
                return obj;
            }
        } catch (Exception e) {
            LOGGER.warn("Couldn't load config! " + clazz.toString());
            LOGGER.warn(e.toString());
        }

        try {
            var obj = clazz.getConstructor().newInstance();
            saveConfig(name, obj);

            return obj;
        } catch (Exception e) {
            LOGGER.error("Invalid config class! " + clazz.toString());
            return null;
        }
    }

    public static void saveConfig(String name, Object obj) {
        try {
            var folder = configDir();
            if (!folder.toFile().isDirectory()) {
                if (folder.toFile().exists()) {
                    Files.deleteIfExists(folder);
                }
                folder.toFile().mkdirs();
            }
            var path = folder.resolve(name + ".json");

            Files.writeString(path, GSON_PRETTY.toJson(obj), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LOGGER.warn("Couldn't save config! " + obj.getClass().toString());
        }
    }

    public static Identifier id(String path) {
        return new Identifier(PolymerUtils.ID, path);
    }

    public static Path getGameDir() {
        return LOADER.getGameDir();
    }
}
