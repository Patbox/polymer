package eu.pb4.polymer.core.impl.networking.payloads;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PolymerGenericListPayload<T>(Type<PolymerGenericListPayload<T>> id, List<T> entries) implements CustomPacketPayload {
    public static <T> StreamCodec<ContextByteBuf, PolymerGenericListPayload<T>> codec(Type<PolymerGenericListPayload<T>> id, StreamCodec<ContextByteBuf, T> codec) {
        return codec.apply(ByteBufCodecs.list()).map(x -> new PolymerGenericListPayload<>(id, x), PolymerGenericListPayload::entries);
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return id;
    }
}
