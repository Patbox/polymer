package eu.pb4.polymer.common.impl;

import net.minecraft.network.ClientConnection;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface CommonNetworkHandlerExt {
    void polymerCommon$setIgnoreNextResourcePack();
    ClientConnection polymerCommon$getConnection();
}
