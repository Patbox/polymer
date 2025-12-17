package eu.pb4.polymer.networking.impl;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public interface ExtCustomPayloadCodec {
    void polymer$setCodecMap(Map<Identifier, StreamCodec<ByteBuf, ?>> codecs);
}
