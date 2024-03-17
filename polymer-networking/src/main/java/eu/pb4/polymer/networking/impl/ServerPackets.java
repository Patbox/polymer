package eu.pb4.polymer.networking.impl;


import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@ApiStatus.Internal
public class ServerPackets {
    public static final HashMap<Identifier, PacketCodec<ByteBuf, ?>> PAYLOAD_CODEC = new HashMap<>();
    public static final Map<Identifier, int[]> VERSION_REGISTRY = new HashMap<>();
    public static final Object2IntOpenHashMap<Identifier> LATEST = new Object2IntOpenHashMap<>();

    public static int getBestSupported(Identifier identifier, int[] ver) {

        var values = VERSION_REGISTRY.get(identifier);

        if (values != null) {
            var verSet = new IntArraySet(ver);

            var value = IntStream.of(values).filter(verSet::contains).max();

            return value.isPresent() ? value.getAsInt() : -1;
        }

        return -1;
    }

    public static void register(Identifier id, PacketCodec<ByteBuf, ?> codec, int... ver) {
        VERSION_REGISTRY.put(id, ver);
        PAYLOAD_CODEC.put(id, codec);
        LATEST.put(id, getBestSupported(id, ver));
    }
}
