package eu.pb4.polymer.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pb4.polymer.impl.client.ClientConfig;
import eu.pb4.polymer.impl.compat.CompatStatus;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class PolymerImpl {

    private PolymerImpl() {
    }

    public static final Logger LOGGER = LoggerFactory.getLogger("Polymer");
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final FabricLoader LOADER = FabricLoader.getInstance();
    public static final boolean DEV_ENV = LOADER.isDevelopmentEnvironment();

    public static final boolean IS_CLIENT = LOADER.getEnvironmentType() == EnvType.CLIENT;

    private static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polymer").get();
    public static final String MOD_ID = CONTAINER.getMetadata().getId();
    public static final String NAME = CONTAINER.getMetadata().getName();
    public static final String DESCRIPTION = CONTAINER.getMetadata().getDescription();
    public static final String[] CONTRIBUTORS;
    public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString().split("\\+")[0];
    public static final String GITHUB_URL = CONTAINER.getMetadata().getContact().get("sources").orElse("https://pb4.eu");

    public static final ServerConfig SERVER_CONFIG = loadConfig("server", ServerConfig.class);

    private static final boolean FORCE_DEVELOPER_MODE = false;
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
    public static final boolean RESEND_BLOCKS_AROUND_CLICK;
    public static final boolean DONT_USE_BLOCK_DELTA_PACKET;
    public static final boolean LOG_SYNC_TIME;
    public static final boolean LOG_BLOCKSTATE_REBUILDS;
    public static final boolean LOG_INVALID_SERVER_IDS_CLIENT;


    public static final Map<String, DisabledMixinReason> DISABLED_MIXINS = new HashMap<>();

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
        DEVELOPER_MODE = DEV_ENV || SERVER_CONFIG.enableDevUtils || FORCE_DEVELOPER_MODE;
        FORCE_RESOURCE_PACK_SERVER = SERVER_CONFIG.markResourcePackAsForcedByDefault;
        FORCE_CUSTOM_MODEL_DATA_OFFSET = SERVER_CONFIG.forcePackOffset || CompatStatus.POLYMC;
        CORE_COMMAND_MINIMAL_OP = SERVER_CONFIG.coreCommandOperatorLevel;
        ADD_NON_POLYMER_CREATIVE_TABS = SERVER_CONFIG.displayNonPolymerCreativeTabs;
        HANDLE_HANDSHAKE_EARLY = SERVER_CONFIG.handleHandshakeEarly;
        RESEND_BLOCKS_AROUND_CLICK = SERVER_CONFIG.sendBlocksAroundClicked;
        DONT_USE_BLOCK_DELTA_PACKET = SERVER_CONFIG.disableChunkDeltaUpdatePacket;
        LOG_SYNC_TIME = DEVELOPER_MODE || SERVER_CONFIG.logHandshakeTime;
        LOG_BLOCKSTATE_REBUILDS = SERVER_CONFIG.logBlockStateRebuilds;

        if (configDir().resolve("mixins.json").toFile().isFile()) {
            for (var mixin : loadConfig("mixins", MixinOverrideConfig.class).disabledMixins) {
                DISABLED_MIXINS.put(mixin, new DisabledMixinReason("Config file (polymer/mixins.json)", "User/config specified, unknown reason"));
            }
        }

        for (var mods : LOADER.getAllMods()) {
            var meta = mods.getMetadata();
            var customValue = meta.getCustomValue("polymer:disable_mixin");

            if (customValue instanceof CustomValue.CvArray cvArray) {
                for (var value : cvArray) {
                    DISABLED_MIXINS.put(value.getAsString(),
                            new DisabledMixinReason(meta.getName() + " (" + meta.getId() + ")", "Unknown reason! I hope author knew what they were doing.."));
                }
            } else if (customValue instanceof CustomValue.CvObject cvObject) {
                for (var value : cvObject) {
                    DISABLED_MIXINS.put(value.getKey(),
                            new DisabledMixinReason(meta.getName() + " (" + meta.getId() + ")", value.getValue().getAsString()));
                }
            }
        }


        if (PolymerImpl.IS_CLIENT) {
            var clientConfig = loadConfig("client", ClientConfig.class);
            USE_ALT_ARMOR_HANDLER = CompatStatus.REQUIRE_ALT_ARMOR_HANDLER || clientConfig.useAlternativeArmorRenderer;
            ENABLE_NETWORKING_CLIENT = clientConfig.enableNetworkSync;
            FORCE_RESOURCE_PACK_CLIENT = clientConfig.forceResourcePackByDefault;
            DISPLAY_DEBUG_INFO_CLIENT = clientConfig.displayF3Info;
            UNLOCK_SERVER_PACK_CLIENT = LOADER.getGameDir().resolve(".polymer_unlock_rp").toFile().exists();
            LOG_INVALID_SERVER_IDS_CLIENT = clientConfig.logInvalidServerEntryIds;
        } else {
            USE_ALT_ARMOR_HANDLER = false;
            ENABLE_NETWORKING_CLIENT = false;
            FORCE_RESOURCE_PACK_CLIENT = false;
            DISPLAY_DEBUG_INFO_CLIENT = false;
            UNLOCK_SERVER_PACK_CLIENT = false;
            LOG_INVALID_SERVER_IDS_CLIENT = false;
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


    public record DisabledMixinReason(String source, String reason) {};
}
