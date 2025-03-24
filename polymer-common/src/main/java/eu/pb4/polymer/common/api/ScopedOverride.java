package eu.pb4.polymer.common.api;

public interface ScopedOverride extends AutoCloseable {
    ScopedOverride NO_OP = () -> {};

    @Override
    void close();
}
