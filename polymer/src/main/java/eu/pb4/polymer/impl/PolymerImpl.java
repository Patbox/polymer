package eu.pb4.polymer.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import eu.pb4.polymer.impl.client.ClientConfig;
import eu.pb4.polymer.impl.compat.CompatStatus;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public final class PolymerImpl {
    private PolymerImpl() {
    }

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final FabricLoader LOADER = FabricLoader.getInstance();

    private static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polymer").get();
    public static final String MOD_ID = CONTAINER.getMetadata().getId();
    public static final String NAME = CONTAINER.getMetadata().getName();
    public static final String DESCRIPTION = CONTAINER.getMetadata().getDescription();
    public static final String[] CONTRIBUTORS;
    public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString().split("\\+")[0];
    public static final String GITHUB_URL = CONTAINER.getMetadata().getContact().get("sources").orElse("https://pb4.eu");

    public static final ServerConfig SERVER_CONFIG = loadConfig("server", ServerConfig.class);

    public static final boolean DEVELOPER_MODE;
    public static final boolean ENABLE_NETWORKING_SERVER;
    public static final boolean ENABLE_NETWORKING_CLIENT;
    public static final boolean USE_ALT_ARMOR_HANDLER;
    public static final boolean FORCE_RESOURCE_PACK_CLIENT;
    public static final boolean FORCE_RESOURCE_PACK_SERVER;
    public static final boolean HANDLE_HANDSHAKE_EARLY;
    public static final boolean FORCE_CUSTOM_MODEL_DATA_OFFSET;
    public static final boolean ENABLE_TEMPLATE_ENTITY_WARNINGS;
    public static final int CORE_COMMAND_MINIMAL_OP;
    public static final boolean DISPLAY_DEBUG_INFO_CLIENT;
    public static final boolean ADD_NON_POLYMER_CREATIVE_TABS;
    public static final boolean UNLOCK_SERVER_PACK_CLIENT;

    static {
        new CompatStatus();

        var list = new ArrayList<String>();
        for (var person : CONTAINER.getMetadata().getAuthors()) {
            list.add(person.getName());
        }
        for (var person : CONTAINER.getMetadata().getContributors()) {
            list.add(person.getName());
        }

        CONTRIBUTORS = list.toArray(new String[0]);


        ENABLE_NETWORKING_SERVER = SERVER_CONFIG.enableNetworkSync;
        ENABLE_TEMPLATE_ENTITY_WARNINGS = SERVER_CONFIG.enableTemplateEntityWarnings;
        DEVELOPER_MODE = FabricLoader.getInstance().isDevelopmentEnvironment() || SERVER_CONFIG.enableDevUtils;
        FORCE_RESOURCE_PACK_SERVER = SERVER_CONFIG.markResourcePackAsForcedByDefault;
        FORCE_CUSTOM_MODEL_DATA_OFFSET = SERVER_CONFIG.forcePackOffset || CompatStatus.POLYMC;
        CORE_COMMAND_MINIMAL_OP = SERVER_CONFIG.coreCommandOperatorLevel;
        ADD_NON_POLYMER_CREATIVE_TABS = SERVER_CONFIG.displayNonPolymerCreativeTabs;
        HANDLE_HANDSHAKE_EARLY = SERVER_CONFIG.handleHandshakeEarly;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            var clientConfig = loadConfig("client", ClientConfig.class);
            USE_ALT_ARMOR_HANDLER = CompatStatus.REQUIRE_ALT_ARMOR_HANDLER || clientConfig.useAlternativeArmorRenderer;
            ENABLE_NETWORKING_CLIENT = clientConfig.enableNetworkSync;
            FORCE_RESOURCE_PACK_CLIENT = clientConfig.forceResourcePackByDefault;
            DISPLAY_DEBUG_INFO_CLIENT = clientConfig.displayF3Info;
            UNLOCK_SERVER_PACK_CLIENT = LOADER.getGameDir().resolve(".polymer_unlock_rp").toFile().exists();
        } else {
            USE_ALT_ARMOR_HANDLER = false;
            ENABLE_NETWORKING_CLIENT = false;
            FORCE_RESOURCE_PACK_CLIENT = false;
            DISPLAY_DEBUG_INFO_CLIENT = false;
            UNLOCK_SERVER_PACK_CLIENT = false;
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


    public static Path getJarPath(String path) {
        return FabricLoader.getInstance().getModContainer(MOD_ID).get().getPath(path);
    }

    public static Path getGameDir() {
        return LOADER.getGameDir();
    }

    public static boolean isOlderThan(String version) {
        try {
            return CONTAINER.getMetadata().getVersion().compareTo(Version.parse(version)) < 0;
        } catch (Exception e) {
            return false;
        }
    }
}
