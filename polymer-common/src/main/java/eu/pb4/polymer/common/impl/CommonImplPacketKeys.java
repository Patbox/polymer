package eu.pb4.polymer.common.impl;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;


public interface CommonImplPacketKeys {
    @Deprecated(forRemoval = true)
    static PacketContext.Key<RegistryAccess> HOLDER_LOOKUP = (PacketContext.Key<RegistryAccess>) PacketContext.REGISTRY_ACCESS;
}
