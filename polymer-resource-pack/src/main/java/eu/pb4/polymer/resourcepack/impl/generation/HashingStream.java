package eu.pb4.polymer.resourcepack.impl.generation;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class HashingStream extends OutputStream {
    private final OutputStream out;
    private final Hasher hasher;
    private final Consumer<HashCode> consumer;

    public HashingStream(OutputStream out, HashFunction hashFunction, Consumer<HashCode> hashCodeConsumer) {
        this.out = out;
        this.hasher = hashFunction.newHasher();
        this.consumer = hashCodeConsumer;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        hasher.putByte((byte) b);
    }

    @Override
    public void write(@NonNull byte[] b) throws IOException {
        out.write(b);
        hasher.putBytes(b);
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        hasher.putBytes(b, off, len);
    }

    @Override
    public void close() throws IOException {
        this.out.close();
        this.consumer.accept(this.hasher.hash());
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }
}
