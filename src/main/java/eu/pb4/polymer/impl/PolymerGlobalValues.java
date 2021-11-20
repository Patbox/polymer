package eu.pb4.polymer.impl;

import eu.pb4.polymer.impl.client.ClientConfig;
import eu.pb4.polymer.impl.compat.CompatStatus;
import net.fabricmc.loader.api.FabricLoader;

public class PolymerGlobalValues {
    public static final boolean DEVELOPER_MODE;
    public static final boolean ENABLE_NETWORKING_SERVER;
    public static final boolean ENABLE_NETWORKING_CLIENT;
    public static final boolean USE_ALT_ARMOR_HANDLER;
    public static final boolean FORCE_RESOURCE_PACK_CLIENT;
    public static final boolean FORCE_RESOURCE_PACK_SERVER;

    static {
        var clientConfig = PolymerMod.loadConfig("client", ClientConfig.class);
        var serverConfig = PolymerMod.loadConfig("server", ServerConfig.class);

        DEVELOPER_MODE = FabricLoader.getInstance().isDevelopmentEnvironment() || serverConfig.enableDevUtils;
        USE_ALT_ARMOR_HANDLER = CompatStatus.REQUIRE_ALT_ARMOR_HANDLER || clientConfig.useAlternativeArmorRenderer;
        ENABLE_NETWORKING_SERVER = serverConfig.enableNetworkSync;
        ENABLE_NETWORKING_CLIENT = clientConfig.enableNetworkSync;
        FORCE_RESOURCE_PACK_CLIENT = clientConfig.forceResourcePackByDefault;
        FORCE_RESOURCE_PACK_SERVER = serverConfig.markResourcePackAsForceByDefault;
    }
}
