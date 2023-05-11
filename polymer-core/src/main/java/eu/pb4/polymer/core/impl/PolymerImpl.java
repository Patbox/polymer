package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.impl.client.ClientConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public final class PolymerImpl {
    public static final Logger LOGGER = CommonImpl.LOGGER;
    private static final FabricLoader LOADER = CommonImpl.LOADER;
    public static final boolean DEV_ENV = CommonImpl.DEV_ENV;

    public static final boolean IS_CLIENT = CommonImpl.IS_CLIENT;

    public static final boolean USE_ALT_ARMOR_HANDLER;
    public static final boolean DISPLAY_DEBUG_INFO_CLIENT;
    public static final boolean ADD_NON_POLYMER_CREATIVE_TABS;
    public static final boolean RESEND_BLOCKS_AROUND_CLICK;
    public static final boolean LOG_SYNC_TIME;
    public static final boolean LOG_BLOCKSTATE_REBUILDS;
    public static final boolean LOG_INVALID_SERVER_IDS_CLIENT;
    public static final boolean CHANGING_QOL_CLIENT;
    public static final boolean USE_UNSAFE_ITEMS_CLIENT;
    public static final boolean SYNC_MODDED_ENTRIES_POLYMC;
    public static final boolean USE_FULL_REI_COMPAT_CLIENT = true;
    public static final boolean LOG_MORE_ERRORS;
    public static final int LIGHT_UPDATE_TICK_DELAY;

    static {
        var serverConfig =  CommonImpl.loadConfig("server", ServerConfig.class);

        ADD_NON_POLYMER_CREATIVE_TABS = serverConfig.displayNonPolymerCreativeTabs;
        RESEND_BLOCKS_AROUND_CLICK = serverConfig.sendBlocksAroundClicked;
        LOG_SYNC_TIME = CommonImpl.DEVELOPER_MODE || serverConfig.logHandshakeTime;
        LOG_BLOCKSTATE_REBUILDS = serverConfig.logBlockStateRebuilds;
        LOG_MORE_ERRORS = CommonImpl.LOG_MORE_ERRORS;
        SYNC_MODDED_ENTRIES_POLYMC = serverConfig.polyMcSyncModdedEntries && CompatStatus.POLYMC;
        LIGHT_UPDATE_TICK_DELAY = serverConfig.lightUpdateTickDelay;


        if (PolymerImpl.IS_CLIENT) {
            var clientConfig =  CommonImpl.loadConfig("client", ClientConfig.class);
            USE_ALT_ARMOR_HANDLER = CompatStatus.REQUIRE_ALT_ARMOR_HANDLER || clientConfig.useAlternativeArmorRenderer;
            DISPLAY_DEBUG_INFO_CLIENT = clientConfig.displayF3Info;
            LOG_INVALID_SERVER_IDS_CLIENT = clientConfig.logInvalidServerEntryIds;
            CHANGING_QOL_CLIENT = !clientConfig.disableNonVisualQualityOfLifeChanges;
            USE_UNSAFE_ITEMS_CLIENT = clientConfig.experimentalModdedContainerSupport;
        } else {
            USE_ALT_ARMOR_HANDLER = false;
            DISPLAY_DEBUG_INFO_CLIENT = false;
            LOG_INVALID_SERVER_IDS_CLIENT = false;
            CHANGING_QOL_CLIENT = false;
            USE_UNSAFE_ITEMS_CLIENT = false;
        }
    }
}
