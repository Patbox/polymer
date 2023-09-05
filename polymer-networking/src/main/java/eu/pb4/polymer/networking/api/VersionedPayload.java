package eu.pb4.polymer.networking.api;

import net.minecraft.network.packet.CustomPayload;

public interface VersionedPayload extends CustomPayload {
    int version();
}
