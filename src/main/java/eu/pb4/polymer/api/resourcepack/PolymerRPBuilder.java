package eu.pb4.polymer.api.resourcepack;


import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

public interface PolymerRPBuilder {
    boolean addData(String path, byte[] data);
    boolean copyModAssets(String modId);
    boolean addCustomModelData(PolymerModelData cmdInfo);
}
