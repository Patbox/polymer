package eu.pb4.polymer.impl.networking.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface BufferWritable {
    void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler);
}
