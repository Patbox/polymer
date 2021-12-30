package eu.pb4.polymer.ext.client.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class ClientImplUtils {

    public static NativeImage generateImage(byte[] bytes) throws IOException {
        return NativeImage.read(new ByteArrayInputStream(bytes));
    }

    public static NativeImageBackedTexture generateTexture(byte[] bytes) throws IOException {
        return new NativeImageBackedTexture(generateImage(bytes));
    }
}
