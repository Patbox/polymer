package eu.pb4.polymer.impl.compat;

import eu.pb4.polymer.impl.PolymerGlobalValues;
import net.fabricmc.loader.api.FabricLoader;

public class CompatStatus {
    private static FabricLoader l = FabricLoader.getInstance();

    public static final boolean FABRIC_SYNC = l.isModLoaded("fabric-registry-sync-v0");

    public static final boolean POLYMC = l.isModLoaded("polymc");
    public static final boolean LITHIUM = l.isModLoaded("lithium");

    public static final boolean WTHIT = l.isModLoaded("wthit");
    public static final boolean REI = l.isModLoaded("roughlyenoughitems");

    public static final boolean IRIS = l.isModLoaded("iris");
    public static final boolean CANVAS = l.isModLoaded("canvas");
    public static final boolean OPTIBAD = l.isModLoaded("optifabric");

    public static final boolean REQUIRE_ALT_ARMOR_HANDLER = IRIS || CANVAS || OPTIBAD;
}
