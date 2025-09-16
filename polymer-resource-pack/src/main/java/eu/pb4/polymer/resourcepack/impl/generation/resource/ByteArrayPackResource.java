package eu.pb4.polymer.resourcepack.impl.generation.resource;

import eu.pb4.polymer.resourcepack.api.PackResource;
import org.jetbrains.annotations.Unmodifiable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public record ByteArrayPackResource(byte[] bytes) implements PackResource {
    @Unmodifiable
    @Override
    public byte[] readAllBytes() {
        return this.bytes;
    }

    @Override
    public InputStream getStream() {
        return new ByteArrayInputStream(this.bytes);
    }
}
