package eu.pb4.polymer.resourcepack.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.BiFunction;

@ApiStatus.NonExtendable
public interface ResourcePackBuilder {
    boolean addData(String path, byte[] data);
    boolean copyAssets(String modId);

    boolean copyFromPath(Path path, String targetPrefix, boolean override);
    default boolean copyFromPath(Path path, String targetPrefix) {
        return this.copyFromPath(path, targetPrefix, true);
    }
    default boolean copyFromPath(Path path) {
        return this.copyFromPath(path, "", true);
    }
    default boolean copyFromPath(Path path, boolean override) {
        return this.copyFromPath(path, "", override);
    }

    boolean addCustomModelData(PolymerModelData itemModel);
    boolean addArmorModel(PolymerArmorModel model);
    @Nullable
    byte[] getData(String path);
    @Nullable
    byte[] getDataOrSource(String path);

    boolean addAssetsSource(String modId);
    void addWriteConverter(BiFunction<String, byte[], @Nullable byte[]> converter);
}
