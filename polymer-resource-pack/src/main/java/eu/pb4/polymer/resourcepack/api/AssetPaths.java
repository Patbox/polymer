package eu.pb4.polymer.resourcepack.api;

import net.minecraft.util.Identifier;

public final class AssetPaths {
    public static String PACK_METADATA = "pack.mcmeta";
    public static String PACK_ICON = "pack.png";

    private AssetPaths() {}

    public static String texture(String namespace, String path) {
        return "assets/" + namespace + "/textures/" + path;
    }

    public static String model(String namespace, String path) {
        return "assets/" + namespace + "/models/" + path;
    }

    public static String model(Identifier id) {
        return model(id.getNamespace(), id.getPath());
    }

    public static String texture(Identifier id) {
        return texture(id.getNamespace(), id.getPath());
    }

    public static String blockModel(Identifier id) {
        return model(id.getNamespace(), "block/" + id.getPath() + ".json");
    }

    public static String blockTexture(Identifier id) {
        return texture(id.getNamespace(), "block/" + id.getPath() + ".png");
    }

    public static String blockTextureMetadata(Identifier id) {
        return texture(id.getNamespace(), "block/" + id.getPath() + ".png.mcmeta");
    }

    public static String itemModel(Identifier id) {
        return model(id.getNamespace(), "item/" + id.getPath() + ".json");
    }

    public static String itemTexture(Identifier id) {
        return texture(id.getNamespace(), "item/" + id.getPath() + ".png");
    }

    public static String itemTextureMetadata(Identifier id) {
        return texture(id.getNamespace(), "item/" + id.getPath() + ".png.mcmeta");
    }

    public static String armorTexture(Identifier id, int layer) {
        return texture(id.getNamespace(), "models/armor/" + id.getPath() + "_layer_" + layer + ".png");
    }

    public static String armorOverlayTexture(Identifier id, int layer) {
        return texture(id.getNamespace(), "models/armor/" + id.getPath() + "_layer_" + layer + "_overlay.png");
    }

    public static String armorTexturePolymerMetadata(Identifier id, int layer) {
        return texture(id.getNamespace(), "models/armor/" + id.getPath() + "_layer_" + layer + ".polymer.json");
    }
}
