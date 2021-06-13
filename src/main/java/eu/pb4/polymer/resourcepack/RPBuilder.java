package eu.pb4.polymer.resourcepack;


public interface RPBuilder {
    boolean addData(String path, byte[] data);
    boolean copyModAssets(String modId);
    boolean addCustomModelData(CMDInfo cmdInfo);


    boolean finish();
}
