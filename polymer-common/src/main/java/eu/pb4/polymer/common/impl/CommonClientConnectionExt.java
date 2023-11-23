package eu.pb4.polymer.common.impl;

import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface CommonClientConnectionExt {
    boolean polymerCommon$hasResourcePack(UUID uuid);

    void polymerCommon$setResourcePack(UUID uuid, boolean value);
    void polymerCommon$setResourcePackNoEvent(UUID uuid, boolean value);
}
