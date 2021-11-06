package eu.pb4.polymer.impl.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class PacketUtils {
    public static PacketByteBuf buf(int version) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        return buf.writeVarInt(version);
    }
}
