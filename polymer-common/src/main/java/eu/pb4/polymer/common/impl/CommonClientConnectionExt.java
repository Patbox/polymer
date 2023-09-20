package eu.pb4.polymer.common.impl;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface CommonClientConnectionExt {
    boolean polymerCommon$hasResourcePack();

    void polymerCommon$setResourcePack(boolean value);
    void polymerCommon$setResourcePackNoEvent(boolean value);
}
