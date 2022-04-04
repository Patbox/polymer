package eu.pb4.polymer.impl.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class CompatStatus {
    private static final FabricLoader LOADER = FabricLoader.getInstance();


    public static final boolean POLYMER_LEGACY = LOADER.isModLoaded("polymer-legacy");
    public static final boolean POLYMER_BLOCKS = LOADER.isModLoaded("polymer-blocks");
    public static final boolean POLYMER_CLIENT_EXT = LOADER.isModLoaded("polymer-client-extensions");

    public static final boolean FABRIC_SYNC = LOADER.isModLoaded("fabric-registry-sync-v0");
    public static final boolean FABRIC_ITEM_GROUP = LOADER.isModLoaded("fabric-item-groups-v0");
    public static final boolean FABRIC_SCREEN_HANDLER = LOADER.isModLoaded("fabric-screen-handler-api-v1");
    public static final boolean FABRIC_PERMISSION_API_V0 = LOADER.isModLoaded("fabric-permissions-api-v0");

    public static final boolean QUILT_ITEM_GROUP = LOADER.isModLoaded("quilt_item_group");

    public static final boolean POLYMC = LOADER.isModLoaded("polymc");
    public static final boolean LITHIUM = LOADER.isModLoaded("lithium");

    public static final boolean WTHIT = LOADER.isModLoaded("wthit");
    public static final boolean REI = LOADER.isModLoaded("roughlyenoughitems");

    public static final boolean IRIS = LOADER.isModLoaded("iris");
    public static final boolean CANVAS = LOADER.isModLoaded("canvas");
    public static final boolean OPTIBAD = LOADER.isModLoaded("optifabric");

    public static final boolean REQUIRE_ALT_ARMOR_HANDLER = IRIS || CANVAS || OPTIBAD;

    public static final boolean IMMERSIVE_PORTALS = LOADER.isModLoaded("imm_ptl_core");

}
