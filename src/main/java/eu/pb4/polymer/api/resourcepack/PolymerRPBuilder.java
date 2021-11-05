package eu.pb4.polymer.api.resourcepack;


import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface PolymerRPBuilder {
    boolean addData(String path, byte[] data);
    boolean copyModAssets(String modId);
    boolean copyFromPath(Path path);
    boolean addCustomModelData(PolymerModelData itemModel);
    boolean addArmorModel(PolymerArmorModel model);
}
