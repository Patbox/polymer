package eu.pb4.polymer.common.impl;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CompatStatus {
    private static final FabricLoader LOADER = FabricLoader.getInstance();

    public static final boolean POLYMER_BLOCKS = LOADER.isModLoaded("polymer-blocks");
    public static final boolean POLYMER_CORE = LOADER.isModLoaded("polymer-core");
    public static final boolean POLYMER_AUTOHOST = LOADER.isModLoaded("polymer-autohost");


    public static final boolean POLYMER_RESOURCE_PACK = LOADER.isModLoaded("polymer-resource-pack");
    @Deprecated
    public static final boolean POLYMER_RESOURCE_PACKS = POLYMER_RESOURCE_PACK;

    public static final boolean FABRIC_SYNC = LOADER.isModLoaded("fabric-registry-sync-v0");
    public static final boolean FABRIC_NETWORKING = LOADER.isModLoaded("fabric-networking-api-v1");
    public static final boolean FABRIC_FLUID_RENDERERING = LOADER.isModLoaded("fabric-rendering-fluids-v1");
    public static final boolean FABRIC_ITEM_GROUP = LOADER.isModLoaded("fabric-item-group-api-v1");
    public static final boolean FABRIC_SCREEN_HANDLER = LOADER.isModLoaded("fabric-screen-handler-api-v1");
    public static final boolean FABRIC_PERMISSION_API_V0 = LOADER.isModLoaded("fabric-permissions-api-v0");

    public static final boolean QUILT_ITEM_GROUP = LOADER.isModLoaded("quilt_item_group");
    public static final boolean QUILT_REGISTRY = LOADER.isModLoaded("quilt_registry");

    public static final boolean POLYMC = LOADER.isModLoaded("polymc");
    public static final boolean LITHIUM = LOADER.isModLoaded("lithium");
    public static final boolean DISGUISELIB = LOADER.isModLoaded("disguiselib");

    public static final boolean WTHIT = LOADER.isModLoaded("wthit");
    public static final boolean JADE = LOADER.isModLoaded("jade");
    public static final boolean REI = LOADER.isModLoaded("roughlyenoughitems");
    public static final boolean JEI = LOADER.isModLoaded("jei");
    public static final boolean EMI = LOADER.isModLoaded("emi");

    public static final boolean FABRIC_PROXY_LITE = LOADER.isModLoaded("fabricproxy-lite");
    public static final boolean FABRIC_PROXY = LOADER.isModLoaded("fabricproxy");
    public static final boolean QFORWARD = LOADER.isModLoaded("qforward");

    public static final boolean PROXY_MODS = FABRIC_PROXY || FABRIC_PROXY_LITE || QFORWARD;

    public static final boolean IRIS = LOADER.isModLoaded("iris");
    public static final boolean CANVAS = LOADER.isModLoaded("canvas");
    public static final boolean OPTIBAD = LOADER.isModLoaded("optifabric");

    public static final boolean REQUIRE_ALT_ARMOR_HANDLER = IRIS || CANVAS || OPTIBAD;

    public static final boolean IMMERSIVE_PORTALS = LOADER.isModLoaded("imm_ptl_core");

}
