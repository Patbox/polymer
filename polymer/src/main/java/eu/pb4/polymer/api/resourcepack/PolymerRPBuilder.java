package eu.pb4.polymer.api.resourcepack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@ApiStatus.NonExtendable
public interface PolymerRPBuilder {
    boolean addData(String path, byte[] data);
    boolean copyModAssets(String modId);
    default boolean copyFromPath(Path path) {
        return this.copyFromPath(path, true);
    }
    boolean copyFromPath(Path path, boolean override);
    boolean addCustomModelData(PolymerModelData itemModel);
    boolean addArmorModel(PolymerArmorModel model);
    @Nullable
    byte[] getData(String path);
}
