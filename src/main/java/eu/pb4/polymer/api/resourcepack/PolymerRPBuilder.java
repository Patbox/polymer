package eu.pb4.polymer.api.resourcepack;

import java.nio.file.Path;

public interface PolymerRPBuilder {
    boolean addData(String path, byte[] data);
    boolean copyModAssets(String modId);
    boolean copyFromPath(Path path);
    boolean addCustomModelData(PolymerModelData itemModel);
    boolean addArmorModel(PolymerArmorModel model);
}
