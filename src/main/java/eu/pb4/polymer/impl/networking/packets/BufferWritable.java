package eu.pb4.polymer.impl.networking.packets;

import net.minecraft.network.PacketByteBuf;

public interface BufferWritable {
    void write(PacketByteBuf buf);
}
