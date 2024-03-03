package eu.pb4.polymer.networking.impl;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UnhandledPayload(Identifier id) implements CustomPayload {

    @Override
    public Id<? extends CustomPayload> getId() {
        return null;
    }
}
