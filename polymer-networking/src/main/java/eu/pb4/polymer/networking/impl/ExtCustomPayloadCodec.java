package eu.pb4.polymer.networking.impl;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface ExtCustomPayloadCodec {
    void polymer$setCodecMap(Map<Identifier, PacketCodec<ByteBuf, ?>> codecs);
}
