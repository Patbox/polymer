package eu.pb4.polymer.impl.resourcepack;

import eu.pb4.polymer.api.resourcepack.PolymerRPBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public interface InternalRPBuilder extends PolymerRPBuilder {
    CompletableFuture<Boolean> buildResourcePack();
}
