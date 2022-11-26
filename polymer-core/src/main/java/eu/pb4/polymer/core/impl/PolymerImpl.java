package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.impl.client.ClientConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class PolymerImpl {
    public static final Logger LOGGER = CommonImpl.LOGGER;
    private static final FabricLoader LOADER = CommonImpl.LOADER;
    public static final boolean DEV_ENV = CommonImpl.DEV_ENV;

    public static final boolean IS_CLIENT = CommonImpl.IS_CLIENT;

    private static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polymer-core").get();
    public static final String MOD_ID = CONTAINER.getMetadata().getId();
    public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString();

    public static final ServerConfig SERVER_CONFIG = loadConfig("server", ServerConfig.class);

    private static final boolean FORCE_DEVELOPER_MODE = false;
    public static final boolean DEVELOPER_MODE;
    public static final boolean ENABLE_NETWORKING_SERVER;
    public static final boolean ENABLE_NETWORKING_CLIENT;
    public static final boolean USE_ALT_ARMOR_HANDLER;
    public static final boolean HANDLE_HANDSHAKE_EARLY;
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
    public static final boolean CHANGING_QOL_CLIENT;
    public static final boolean USE_UNSAFE_ITEMS_CLIENT;
    public static final boolean USE_FULL_REI_COMPAT_CLIENT = true;
    public static final boolean LOG_MORE_ERRORS;

    static {
        ENABLE_NETWORKING_SERVER = SERVER_CONFIG.enableNetworkSync;
        ENABLE_TEMPLATE_ENTITY_WARNINGS = SERVER_CONFIG.enableTemplateEntityWarnings;
        DEVELOPER_MODE = DEV_ENV || SERVER_CONFIG.enableDevUtils || FORCE_DEVELOPER_MODE;
        CORE_COMMAND_MINIMAL_OP = SERVER_CONFIG.coreCommandOperatorLevel;
        ADD_NON_POLYMER_CREATIVE_TABS = SERVER_CONFIG.displayNonPolymerCreativeTabs;
        HANDLE_HANDSHAKE_EARLY = SERVER_CONFIG.handleHandshakeEarly;
        RESEND_BLOCKS_AROUND_CLICK = SERVER_CONFIG.sendBlocksAroundClicked;
        DONT_USE_BLOCK_DELTA_PACKET = SERVER_CONFIG.disableChunkDeltaUpdatePacket;
        LOG_SYNC_TIME = DEVELOPER_MODE || SERVER_CONFIG.logHandshakeTime;
        LOG_BLOCKSTATE_REBUILDS = SERVER_CONFIG.logBlockStateRebuilds;
        LOG_MORE_ERRORS = SERVER_CONFIG.logAllExceptions || DEVELOPER_MODE;


        if (PolymerImpl.IS_CLIENT) {
            var clientConfig = loadConfig("client", ClientConfig.class);
            USE_ALT_ARMOR_HANDLER = CompatStatus.REQUIRE_ALT_ARMOR_HANDLER || clientConfig.useAlternativeArmorRenderer;
            ENABLE_NETWORKING_CLIENT = clientConfig.enableNetworkSync;
            DISPLAY_DEBUG_INFO_CLIENT = clientConfig.displayF3Info;
            UNLOCK_SERVER_PACK_CLIENT = LOADER.getGameDir().resolve(".polymer_unlock_rp").toFile().exists();
            LOG_INVALID_SERVER_IDS_CLIENT = clientConfig.logInvalidServerEntryIds;
            CHANGING_QOL_CLIENT = !clientConfig.disableNonVisualQualityOfLifeChanges;
            USE_UNSAFE_ITEMS_CLIENT = clientConfig.experimentalModdedContainerSupport;
        } else {
            USE_ALT_ARMOR_HANDLER = false;
            ENABLE_NETWORKING_CLIENT = false;
            DISPLAY_DEBUG_INFO_CLIENT = false;
            UNLOCK_SERVER_PACK_CLIENT = false;
            LOG_INVALID_SERVER_IDS_CLIENT = false;
            CHANGING_QOL_CLIENT = false;
            USE_UNSAFE_ITEMS_CLIENT = false;
        }
    }

    public static <T> T loadConfig(String name, Class<T> clazz) {
        return CommonImpl.loadConfig(name, clazz);
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
