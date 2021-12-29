package eu.pb4.polymer.resourcepack;


@Deprecated
public interface RPBuilder {
    boolean addData(String path, byte[] data);
    boolean copyModAssets(String modId);
    boolean addCustomModelData(CMDInfo cmdInfo);


    default boolean finish() {
        return false;
    }
}
