package eu.pb4.polymer.resourcepack.impl.generation;

import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public interface InternalRPBuilder<T> extends ResourcePackBuilder {
    CompletableFuture<T> buildResourcePack();
}
