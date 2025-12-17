package eu.pb4.polymer.common.impl;

import net.minecraft.network.Connection;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface CommonPacketListenerImplExt {
    void polymerCommon$setIgnoreNextResourcePack();
    Connection polymerCommon$getConnection();
}
