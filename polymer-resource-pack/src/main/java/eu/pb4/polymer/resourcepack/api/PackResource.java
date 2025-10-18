package eu.pb4.polymer.resourcepack.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import eu.pb4.polymer.resourcepack.impl.generation.resource.ByteArrayPackResource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@ApiStatus.NonExtendable
public interface PackResource {
    byte[] readAllBytes();
    InputStream getStream();

    static PackResource of(byte[] t) {
        return new ByteArrayPackResource(t);
    }

    @Nullable
    default String asString() {
        return new String(readAllBytes(), StandardCharsets.UTF_8);
    }

    @Nullable
    default JsonElement asJson() {
        try {
            return JsonParser.parseString(asString());
        } catch (Throwable e) {
            return null;
        }
    }

    @Nullable
    default BufferedImage asImage() {
        try {
            return ImageIO.read(getStream());
        } catch (IOException e) {
            return null;
        }
    }

    static PackResource fromString(String string) {
        return new ByteArrayPackResource(string.getBytes(StandardCharsets.UTF_8));
    }

    static PackResource fromAsset(WritableAsset asset) {
        return new ByteArrayPackResource(asset.toBytes());
    }

    static PackResource fromJson(JsonElement jsonElement) {
        return fromString(jsonElement.toString());
    }

    static PackResource fromImage(BufferedImage image) {
        var b = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayPackResource(b.toByteArray());
    }

    static PackResource of(InputStream stream) throws IOException {
        // Todo: optimize
        return new ByteArrayPackResource(stream.readAllBytes());
    }

    static PackResource of(Path path) throws IOException {
        // Todo: optimize
        return new ByteArrayPackResource(Files.readAllBytes(path));
    }
}
