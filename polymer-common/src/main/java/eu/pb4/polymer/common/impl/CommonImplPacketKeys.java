package eu.pb4.polymer.common.impl;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;


public interface CommonImplPacketKeys {
    static PacketContext.Key<RegistryAccess> HOLDER_LOOKUP = PacketContext.key(CommonImplUtils.id("holder_lookup"));
}
