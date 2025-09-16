package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.resourcepack.impl.generation.resource.ByteArrayPackResource;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@ApiStatus.NonExtendable
public interface PackResource {
    byte[] readAllBytes();
    InputStream getStream();

    static PackResource of(byte[] t) {
        return new ByteArrayPackResource(t);
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
