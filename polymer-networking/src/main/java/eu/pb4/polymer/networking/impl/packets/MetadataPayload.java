package eu.pb4.polymer.networking.impl.packets;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MetadataPayload(Map<Identifier, Tag> map) implements CustomPacketPayload {
    public static final Type<MetadataPayload> ID = PolymerNetworking.id("polymer", "metadata");
    public static final StreamCodec<ContextByteBuf, MetadataPayload> CODEC = StreamCodec.ofMember(MetadataPayload::write, MetadataPayload::read);
    public void write(ContextByteBuf buf) {
        buf.writeMap(map, FriendlyByteBuf::writeIdentifier, (x, n) -> x.writeNbt(n));
    }

    public static MetadataPayload read(ContextByteBuf buf) {
        return new MetadataPayload(buf.readMap(FriendlyByteBuf::readIdentifier, (bufx) -> bufx.readNbt(NbtAccounter.unlimitedHeap())));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
