package eu.pb4.polymer.impl.compat;

import eu.pb4.polymer.impl.PolymerImpl;

public class CompatStatus {
    public static final boolean FABRIC_SYNC = PolymerImpl.isModLoaded("fabric-registry-sync-v0");
    public static final boolean FABRIC_PERMISSION_API_V0 = PolymerImpl.isModLoaded("fabric-permissions-api-v0");

    public static final boolean POLYMC = PolymerImpl.isModLoaded("polymc");
    public static final boolean LITHIUM = PolymerImpl.isModLoaded("lithium");

    public static final boolean WTHIT = PolymerImpl.isModLoaded("wthit");
    public static final boolean REI = PolymerImpl.isModLoaded("roughlyenoughitems");

    public static final boolean IRIS = PolymerImpl.isModLoaded("iris");
    public static final boolean CANVAS = PolymerImpl.isModLoaded("canvas");
    public static final boolean OPTIBAD = PolymerImpl.isModLoaded("optifabric");

    public static final boolean REQUIRE_ALT_ARMOR_HANDLER = IRIS || CANVAS || OPTIBAD;

    public static final boolean IMMERSIVE_PORTALS = PolymerImpl.isModLoaded("imm_ptl_core");

}
