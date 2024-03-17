package eu.pb4.polymer.core.impl.networking.payloads;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PolymerGenericListPayload<T>(Id<PolymerGenericListPayload<T>> id, List<T> entries) implements CustomPayload {
    public static <T> PacketCodec<ContextByteBuf, PolymerGenericListPayload<T>> codec(Id<PolymerGenericListPayload<T>> id, PacketCodec<ContextByteBuf, T> codec) {
        return codec.collect(PacketCodecs.toList()).xmap(x -> new PolymerGenericListPayload<>(id, x), PolymerGenericListPayload::entries);
    }
    @Override
    public Id<? extends CustomPayload> getId() {
        return id;
    }
}
