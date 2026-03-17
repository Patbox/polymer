package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface OutputGenerator<T> {
    T generateFile(List<Map.Entry<String, PackResource>> resources, ResourcePackBuilder.ResourceConverter converter, ResourcePackStatusConsumer status);

    static OutputGenerator<Result> zipGenerator(Path out) {
        return (a, b, c) -> DefaultRPBuilder.writeSingleZip(out, a, b, c);
    }

    record Result(Path path, String hash, boolean hadIssues) {}
}
