package eu.pb4.polymer.rsm.impl;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CompatStatus {
    private static final FabricLoader LOADER = FabricLoader.getInstance();

    public static final boolean FABRIC_SYNC = LOADER.isModLoaded("fabric-registry-sync-v0");

    public static final boolean QUILT_REGISTRY = LOADER.isModLoaded("quilt_registry");
}
